package fr.redteam.util;

/**
 * Codes ANSI pour couleurs et formatage du terminal.
 */
public final class Ansi {

    public static final String RESET = "\033[0m";
    public static final String BOLD = "\033[1m";
    public static final String DIM = "\033[2m";

    public static final String RED = "\033[31m";
    public static final String GREEN = "\033[32m";
    public static final String YELLOW = "\033[33m";
    public static final String BLUE = "\033[34m";
    public static final String MAGENTA = "\033[35m";
    public static final String CYAN = "\033[36m";
    public static final String WHITE = "\033[37m";

    public static final String BG_RED = "\033[41m";
    public static final String BG_GREEN = "\033[42m";

    private Ansi() {}

    public static String red(String s) { return RED + s + RESET; }
    public static String green(String s) { return GREEN + s + RESET; }
    public static String yellow(String s) { return YELLOW + s + RESET; }
    public static String blue(String s) { return BLUE + s + RESET; }
    public static String cyan(String s) { return CYAN + s + RESET; }
    public static String magenta(String s) { return MAGENTA + s + RESET; }
    public static String bold(String s) { return BOLD + s + RESET; }
    public static String dim(String s) { return DIM + s + RESET; }
}
