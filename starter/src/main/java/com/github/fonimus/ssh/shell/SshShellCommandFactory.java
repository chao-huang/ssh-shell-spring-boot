package com.github.fonimus.ssh.shell;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.jline.reader.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.Banner;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.JLineShellAutoConfiguration;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

import static com.github.fonimus.ssh.shell.SshShellHistoryAutoConfiguration.HISTORY_FILE;

/**
 * Ssh shell command implementation, which starts threads of SshShellRunnable
 *
 * @see SshShellRunnable
 */
@Slf4j
@Component
public class SshShellCommandFactory
		implements Command {

	public static final ThreadLocal<SshContext> SSH_THREAD_CONTEXT = ThreadLocal.withInitial(() -> null);

	private Banner shellBanner;

	private PromptProvider promptProvider;

	private Shell shell;

	private JLineShellAutoConfiguration.CompleterAdapter completerAdapter;

	private final Parser parser;

	private Environment environment;

	private File historyFile;

	private boolean displayBanner;

	public static final ThreadLocal<SshIO> SSH_IO_CONTEXT = ThreadLocal.withInitial(SshIO::new);

	private Map<ChannelSession, Thread> threads = new ConcurrentHashMap<>();

	/**
	 * Constructor
	 *
	 * @param banner           shell banner
	 * @param promptProvider   prompt provider
	 * @param shell            spring shell
	 * @param completerAdapter completer adapter
	 * @param parser           jline parser
	 * @param environment      spring environment
	 * @param historyFile      history file location
	 * @param properties       ssh shell properties
	 */
	public SshShellCommandFactory(@Autowired(required = false) Banner banner, @Lazy PromptProvider promptProvider, Shell shell,
			JLineShellAutoConfiguration.CompleterAdapter completerAdapter, Parser parser, Environment environment,
			@Qualifier(HISTORY_FILE) File historyFile, SshShellProperties properties) {
		this.shellBanner = banner;
		this.promptProvider = promptProvider;
		this.shell = shell;
		this.completerAdapter = completerAdapter;
		this.parser = parser;
		this.environment = environment;
		this.historyFile = historyFile;
		this.displayBanner = properties.isDisplayBanner();
	}

	/**
	 * Start ssh session
	 *
	 * @param channelSession ssh channel session
	 * @param env            ssh environment
	 */
	@Override
	public void start(ChannelSession channelSession, org.apache.sshd.server.Environment env) {
		SshIO sshIO = SSH_IO_CONTEXT.get();
		Thread sshThread = new Thread(new ThreadGroup("ssh-shell"),
				new SshShellRunnable(channelSession, shellBanner, promptProvider, shell, completerAdapter, parser, environment, historyFile, env,
						displayBanner, this, sshIO.getIs(), sshIO.getOs(), sshIO.getEc()),
				"ssh-session-" + System.nanoTime());
		sshThread.start();
		threads.put(channelSession, sshThread);
		LOGGER.debug("{}: started [{} session(s) currently active]", channelSession, threads.size());
	}

	@Override
	public void destroy(ChannelSession channelSession) {
		Thread sshThread = threads.remove(channelSession);
		if (sshThread != null) {
			sshThread.interrupt();
		}
		LOGGER.debug("{}: destroyed [{} session(s) currently active]", channelSession, threads.size());
	}

	@Override
	public void setErrorStream(OutputStream errOS) {
		// not used
	}

	@Override
	public void setExitCallback(ExitCallback ec) {
		SSH_IO_CONTEXT.get().setEc(ec);
	}

	@Override
	public void setInputStream(InputStream is) {
		SSH_IO_CONTEXT.get().setIs(is);
	}

	@Override
	public void setOutputStream(OutputStream os) {
		SSH_IO_CONTEXT.get().setOs(os);
	}
}
