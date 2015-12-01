package com.afome.ChatBot;

import java.util.ArrayList;
import java.util.HashMap;

public class EBNamingOption {
    public static final int minCharacters = 4;
    public static final int maxCharacters = 7;
    public static final int maxMovement = 20;
    public static final String invalidCharacterReason = "This option contains an invalid character (%s)";
    public static final String excessMovementsReason = "This option contains too many movements (%s > " + String.valueOf(maxMovement) + ")";
    public static final String invalidCharacterCount = "This option contains an invalid number of characters (%s)";


    //Contains the number of cursor movements to get to each character
    //in the Earthbound character naming screen
    public static final HashMap<String, Integer> characterMappings;
    static {
        characterMappings = new HashMap<String, Integer>();
        characterMappings.put("A", 0);
        characterMappings.put("B", 1);
        characterMappings.put("C", 2);
        characterMappings.put("D", 3);
        characterMappings.put("E", 4);
        characterMappings.put("F", 5);
        characterMappings.put("G", 5);
        characterMappings.put("H", 4);
        characterMappings.put("I", 3);
        characterMappings.put("J", 1);
        characterMappings.put("K", 2);
        characterMappings.put("L", 3);
        characterMappings.put("M", 4);
        characterMappings.put("N", 5);
        characterMappings.put("O", 6);
        characterMappings.put("P", 6);
        characterMappings.put("Q", 5);
        characterMappings.put("R", 4);
        characterMappings.put("S", 2);
        characterMappings.put("T", 3);
        characterMappings.put("U", 4);
        characterMappings.put("V", 5);
        characterMappings.put("W", 6);
        characterMappings.put("X", 7);
        characterMappings.put("Y", 7);
        characterMappings.put("Z", 6);
        characterMappings.put("0", 3);
        characterMappings.put("1", 4);
        characterMappings.put("2", 5);
        characterMappings.put("3", 4);
        characterMappings.put("4", 5);
        characterMappings.put("5", 6);
        characterMappings.put("6", 7);
        characterMappings.put("7", 8);
        characterMappings.put("8", 7);
        characterMappings.put("9", 6);
        characterMappings.put(".", 2);
        characterMappings.put("BU", 1);
        characterMappings.put("'", 3);
        characterMappings.put("~", 2);
        characterMappings.put(".", 4);
        characterMappings.put("/", 3);
        characterMappings.put("!", 5);
        characterMappings.put("MN", 4);
        characterMappings.put("?", 4);
        characterMappings.put("OH", 3);
        characterMappings.put("BL", 5);

    }

    private ArrayList<String> characters;
    private String invalidReason = null;
    private boolean valid = false;
    private int movementCount = -1;


    public EBNamingOption(ArrayList<String> characters) {
        this.characters = characters;
        checkValidity();
    }

    private void checkValidity() {
        if (characters.size() >= minCharacters && characters.size() <= maxCharacters) {
            movementCount = 0;
            for (String character : characters) {
                if (!characterMappings.containsKey(character)) {
                    invalidReason = String.format(invalidCharacterReason, character);
                    valid = false;
                    return;
                } else {
                    movementCount += characterMappings.get(character);
                }
            }
            if (movementCount> maxMovement) {
                invalidReason = String.format(excessMovementsReason, String.valueOf(movementCount));
            }
            valid = (movementCount <= maxMovement);
        } else {
            invalidReason = String.format(invalidCharacterCount, String.valueOf(characters.size()));
            valid = false;
            return;
        }
    }

    public boolean isValid() {
        return valid;
    }

    //Will return null if this naming option is valid
    public String getInvalidReason() {
        return invalidReason;
    }

    public int getMovementCount() {
        return movementCount;
    }
}
