package com.github.fonimus.ssh.shell.commands;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.github.fonimus.ssh.shell.postprocess.PostProcessor;

/**
 * Command to list available post processors
 */
@ShellComponent
@ShellCommandGroup("Built-In Commands")
public class Postprocessors {

	private List<PostProcessor> postProcessors;

	public Postprocessors(List<PostProcessor> postProcessors) {
		this.postProcessors = new ArrayList<>(postProcessors);
		this.postProcessors.sort(Comparator.comparing(PostProcessor::getName));
	}

	@ShellMethod(value = "Display the available post processors")
	public CharSequence postprocessors() {
		AttributedStringBuilder result = new AttributedStringBuilder();
		result.append("Available Post-Processors\n\n", AttributedStyle.BOLD);
		for (PostProcessor postProcessor : postProcessors) {
			result.append("\t" + postProcessor.getName() + ": ", AttributedStyle.BOLD);
			Class<?> cls = ((Class) ((ParameterizedType) (postProcessor.getClass().getGenericInterfaces())[0]).getActualTypeArguments()[0]);
			result.append(cls.getName() + "\n", AttributedStyle.DEFAULT);
		}

		return result;
	}

}
