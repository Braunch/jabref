package net.sf.jabref.model.util;

import java.util.Map;

public class LatexToUnicode {

    private static final Map<String, String> CHARS = HTMLUnicodeConversionMaps.LATEX_UNICODE_CONVERSION_MAP;
    private static final Map<String, String> ACCENTS = HTMLUnicodeConversionMaps.UNICODE_ESCAPED_ACCENTS;


    public String format(String inField) {
        if (inField.isEmpty()) {
            return "";
        }
        int i;
        // TODO: document what does this do
        String field = inField.replaceAll("&|\\\\&", "&amp;").replaceAll("[\\n]{1,}", "<p>").replace("\\$", "&dollar;") // Replace \$ with &dollar;
                .replaceAll("\\$([^\\$]*)\\$", "\\{$1\\}");

        StringBuilder sb = new StringBuilder();
        StringBuilder currentCommand = null;

        char c;
        boolean escaped = false;
        boolean incommand = false;

        for (i = 0; i < field.length(); i++) {
            c = field.charAt(i);
            if (escaped && (c == '\\')) {
                sb.append('\\');
                escaped = false;
            } else if (c == '\\') {
                if (incommand) {
                    /* Close Command */
                    String command = currentCommand.toString();
                    String result = CHARS.get(command);
                    if (result == null) {
                        sb.append(command);
                    } else {
                        sb.append(result);
                    }

                }
                escaped = true;
                incommand = true;
                currentCommand = new StringBuilder();
            } else if (!incommand && ((c == '{') || (c == '}'))) {
                // Swallow the brace.
            } else if (Character.isLetter(c) || (c == '%')
                    || ModelStringUtil.SPECIAL_COMMAND_CHARS.contains(String.valueOf(c))) {
                escaped = false;

                if (!incommand) {
                    sb.append(c);
                } else {
                    currentCommand.append(c);
                    if ((currentCommand.length() == 1)
                            && ModelStringUtil.SPECIAL_COMMAND_CHARS.contains(currentCommand.toString())
                            && !(i >= (field.length() - 1))) {
                        // This indicates that we are in a command of the type
                        // \^o or \~{n}

                        String command = currentCommand.toString();
                        i++;
                        c = field.charAt(i);
                        String commandBody;
                        if (c == '{') {
                            String part = ModelStringUtil.getPart(field, i, false);
                            i += part.length();
                            commandBody = part;
                        } else {
                            commandBody = field.substring(i, i + 1);
                        }
                        String result = CHARS.get(command + commandBody);

                        if (result == null) {
                            // Use combining accents if argument is single character or empty
                            if (commandBody.length() <= 1) {
                                String accent = ACCENTS.get(command);
                                if (accent == null) {
                                    // Shouldn't happen
                                    sb.append(commandBody);
                                } else {
                                    sb.append(commandBody).append(accent);
                                }
                            }
                        } else {
                            sb.append(result);
                        }

                        incommand = false;
                        escaped = false;
                    } else {
                        //  Are we already at the end of the string?
                        if ((i + 1) == field.length()) {
                            String command = currentCommand.toString();
                            String result = CHARS.get(command);
                            /* If found, then use translated version. If not,
                             * then keep
                             * the text of the parameter intact.
                             */
                            if (result == null) {
                                sb.append(command);
                            } else {
                                sb.append(result);
                            }

                        }
                    }
                }
            } else {
                if (!incommand) {
                    sb.append(c);
                } else if (Character.isWhitespace(c) || (c == '{') || (c == '}')) {
                    // First test if we are already at the end of the string.
                    // if (i >= field.length()-1)
                    // break testContent;

                    String command = currentCommand.toString();

                    if (c == '{') {
                        String argument = ModelStringUtil.getPart(field, i, true);
                        i += argument.length();
                        // handle common case of general latex command
                        String result = CHARS.get(command + argument);

                        // If found, then use translated version. If not, then keep
                        // the
                        // text of the parameter intact.
                        if (result == null) {
                            // Use combining accents if argument is single character or empty
                            if (argument.length() <= 1) {
                                String accent = ACCENTS.get(command);
                                if (accent == null) {
                                    if (argument.isEmpty()) {
                                        // Empty argument, may be used as separator as in \LaTeX{}, so keep the command
                                        sb.append(command);
                                    } else {
                                        sb.append(argument);
                                    }
                                } else {
                                    sb.append(argument).append(accent);
                                }
                            } else {
                                sb.append(argument);
                            }
                        } else {
                            sb.append(result);
                        }
                    } else if (c == '}') {
                        // This end brace terminates a command. This can be the case in
                        // constructs like {\aa}. The correct behaviour should be to
                        // substitute the evaluated command and swallow the brace:
                        String result = CHARS.get(command);

                        if (result == null) {
                            // If the command is unknown, just print it:
                            sb.append(command);
                        } else {
                            sb.append(result);
                        }

                    } else {
                        String result = CHARS.get(command);

                        if (result == null) {
                            sb.append(command);
                        } else {
                            sb.append(result);
                        }
                        sb.append(' ');
                    }
                } else {
                    /*
                     * TODO: this point is reached, apparently, if a command is
                     * terminated in a strange way, such as with "$\omega$".
                     * Also, the command "\&" causes us to get here. The former
                     * issue is maybe a little difficult to address, since it
                     * involves the LaTeX math mode. We don't have a complete
                     * LaTeX parser, so maybe it's better to ignore these
                     * commands?
                     */
                }

                incommand = false;
                escaped = false;
            }
        }

        return sb.toString().replace("&amp;", "&").replace("<p>", "\n").replace("&dollar;", "$").replace("~",
                "\u00A0");

    }
}
