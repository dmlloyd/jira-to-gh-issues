/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.pivotal.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.pivotal.jira.JiraConfig;
import io.pivotal.jira.JiraUser;
import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Rob Winch
 *
 */
@Data
@Component
public class MarkdownEngine implements MarkupEngine {

	private static final Pattern userPattern = Pattern.compile("\\[~([^\\]]+)\\]");

	String jiraBaseUrl;

	private Map<String, JiraUser> userLookup = new HashMap<>();


	@Autowired
	public void setJiraConfig(JiraConfig jiraConfig) {
		jiraBaseUrl = jiraConfig.getBaseUrl();
	}

	@Override
	public void configureUserLookup(Map<String, JiraUser> userLookup) {
		this.userLookup.putAll(userLookup);
	}

	@Override
	public String link(String text, String href) {
		return "["+text+"]("+href+")";
	}

	@Override
	public String convert(String text) {
		if(!StringUtils.hasLength(text)) {
			return "";
		}
		text = text.replaceAll("(?m)^[ \\t]*# ", "- ");
		text = text.replaceAll("(?m)^[ \\t]*\\* ", "- ");
		text = header(text);
		text = text.replaceAll("\\{(code|noformat)(:(\\w+))?(?:(:|\\|)\\w+=.+?)*\\}","```$3 ");
		text = text.replaceAll("(```\\w*) (.+)", "$1\n$2");
		text = text.replaceAll("(.)(```) ", "$1\n$2");
		text = text.replaceAll("\\[(.+?)\\|(http.*?)\\]","[$1]($2)");
		text = text.replaceAll("\\{\\{(.+?)\\}\\}","`$1`");
		text = quote(text);
		text = text.replaceAll("(?m)^[ \\t]*bq\\.", "> ");
		text = text.replaceAll("\\{(color)(:(\\w+))?(?:(:|\\|)\\w+=.+?)*\\}","**");
		text = replaceUserMentions(text);
		return text;
	}

	private String replaceUserMentions(String text) {
		Matcher matcher = userPattern.matcher(text);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String key = matcher.group(1);
			JiraUser user = this.userLookup.getOrDefault(key, createUser(key));
			matcher.appendReplacement(sb, "[" + user.getDisplayName() + "](" + user.getBrowserUrl() + ")");
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private JiraUser createUser(String key) {
		JiraUser user = new JiraUser();
		user.setKey(key);
		user.setDisplayName(key);
		user.setSelf(this.jiraBaseUrl);
		return user;
	}

	public String header(String text) {
		text = text.replaceAll("^h1. ", "# ");
		text = text.replaceAll("^h2. ", "## ");
		text = text.replaceAll("^h3. ", "### ");
		text = text.replaceAll("^h4. ", "#### ");
		text = text.replaceAll("^h5. ", "##### ");
		text = text.replaceAll("^h6. ", "###### ");
		return text;
	}

	public static String quote(String str) {
		String[] parts = str.split("\\{quote\\}");

		for(int i=1;i<parts.length;i+=2) {
		    parts[i] = "\n > " + parts[i].replaceAll("\n","\n> ");
		}
		return StringUtils.arrayToDelimitedString(parts, "");
	}
}
