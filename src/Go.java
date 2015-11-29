import wci.frontend.GoParser;

public class Go
{
    public static void main(String[] args)
    {
        try {
            GoParser.main(args);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}