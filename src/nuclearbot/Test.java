package nuclearbot;

import nuclearbot.util.ArgumentFormatter;

public class Test {

    public static void main(String[] args)
    {
        String format = "$0 hugs $1";
        ArgumentFormatter formatter = new ArgumentFormatter(format);

        String message = formatter.format("Player", "", "Target");

        System.out.println(message);
    }

}
