import wci.frontend.ProlangParser;

public class Prolang
{
    public static void main(String[] args)
    {
        try {
            ProlangParser.main(args);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}