package com.github.fonimus.ssh.shell.sample;

import com.github.fonimus.ssh.shell.SshShellUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

/**
 * Demo command for example
 */
@ShellComponent
public class DemoCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoCommand.class);

    /**
     * Echo command
     *
     * @param message message to print
     * @return message
     */
    @ShellMethod("Echo command")
    public String echo(String message) {
        return message;
    }

    /**
     * Ex command
     *
     * @throws IllegalStateException for example
     */
    @ShellMethod("Ex command")
    public void ex() {
        throw new IllegalStateException("Test exception message");
    }

    /**
     * Interaction example command
     *
     * @return welcome message
     */
    @ShellMethod("Welcome command")
    public String welcome() {
        String name = SshShellUtils.read("What's your name ?");
        return "Hello, '" + name + "' !";
    }

    /**
     * Confirmation example command
     *
     * @return welcome message
     */
    @ShellMethod("Confirmation command")
    public String conf() {
        return SshShellUtils.confirm("Are you sure ?") ? "Great ! Let's do it !" : "Such a shame ...";
    }


    /**
     * For scheduled command example
     */
    @Scheduled(initialDelay = 0, fixedDelay = 60000)
    public void log() {
        LOGGER.info("In scheduled task..");
    }
}
