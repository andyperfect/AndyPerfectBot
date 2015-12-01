package test.com.afome;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;
import com.afome.ChatBot.EBNamingOption;

import java.util.ArrayList;
import java.util.Arrays;

public class EBNamingOptionTest {

    @Before
    public void before() throws Exception {
        System.out.println("Beginning EBNamingOptionTest");
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testValidNamingOptions() throws Exception {
        EBNamingOption option1 = new EBNamingOption(new ArrayList<String>(Arrays.asList("A", "B", "C", "D")));
        Assert.assertTrue(option1.isValid());
        Assert.assertTrue(option1.getMovementCount() == 6);

        EBNamingOption option2 = new EBNamingOption(new ArrayList<String>(Arrays.asList("BL", "BL", "BL", "BL")));
        Assert.assertTrue(option2.isValid());
        Assert.assertTrue(option2.getMovementCount() == 20);

        EBNamingOption option3 = new EBNamingOption(new ArrayList<String>(Arrays.asList("BL", "BL", "BL", "BL", "A")));
        Assert.assertTrue(option3.isValid());
        Assert.assertTrue(option3.getMovementCount() == 20);

        EBNamingOption option4 = new EBNamingOption(new ArrayList<String>(Arrays.asList("V", "V", "V", "BL", "A", "A")));
        Assert.assertTrue(option4.isValid());
        Assert.assertTrue(option4.getMovementCount() == 20);

        EBNamingOption option5 = new EBNamingOption(new ArrayList<String>(Arrays.asList("H", "OH", "3", "!", "B", "A", "A")));
        Assert.assertTrue(option5.isValid());
        Assert.assertTrue(option5.getMovementCount() == 17);
    }

    @Test
    public void testInvalidNamingOptions() throws Exception {
        EBNamingOption option1 = new EBNamingOption(new ArrayList<String>(Arrays.asList("AH", "A", "B", "MN", "U", "A", "A")));
        Assert.assertTrue(!option1.isValid());
        Assert.assertTrue(option1.getInvalidReason().contains("invalid character") && option1.getInvalidReason().contains("(AH)"));

        EBNamingOption option2 = new EBNamingOption(new ArrayList<String>(Arrays.asList("B", "A", "L", "<")));
        Assert.assertTrue(!option2.isValid());
        Assert.assertTrue(option2.getInvalidReason().contains("invalid character") && option2.getInvalidReason().contains("(<)"));

        EBNamingOption option3 = new EBNamingOption(new ArrayList<String>(Arrays.asList("I", "O", "C")));
        Assert.assertTrue(!option3.isValid());
        Assert.assertTrue(option3.getInvalidReason().contains("invalid number of characters (3)"));

        EBNamingOption option4 = new EBNamingOption(new ArrayList<String>(Arrays.asList("I", "O", "C", "C", "D", "R", "A", "B")));
        Assert.assertTrue(!option4.isValid());
        Assert.assertTrue(option4.getInvalidReason().contains("invalid number of characters (8)"));

        EBNamingOption option5 = new EBNamingOption(new ArrayList<String>(Arrays.asList("P", "O", "O", "P")));
        Assert.assertTrue(!option5.isValid());
        Assert.assertTrue(option5.getInvalidReason().contains("too many movements (24 > " + EBNamingOption.maxMovement + ")"));

        EBNamingOption option6 = new EBNamingOption(new ArrayList<String>(Arrays.asList("B", "A", "R", "T", "7", "7")));
        Assert.assertTrue(!option6.isValid());
        Assert.assertTrue(option6.getInvalidReason().contains("too many movements (24 > " + EBNamingOption.maxMovement + ")"));

        EBNamingOption option7 = new EBNamingOption(new ArrayList<String>(Arrays.asList("W", "O", "O", "L")));
        Assert.assertTrue(!option7.isValid());
        Assert.assertTrue(option7.getInvalidReason().contains("too many movements (21 > " + EBNamingOption.maxMovement + ")"));
    }
}