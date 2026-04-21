package com.tournament.factory;

import com.tournament.model.Sport;
import com.tournament.model.Cricket;
import com.tournament.model.Football;
import com.tournament.model.Badminton;
/**
 * FACTORY PATTERN
 * ───────────────
 * Encapsulates Sport object creation.
 * Callers only know the Sport interface; concrete types are hidden.
 *
 * Usage:
 *   Sport s = SportFactory.createSport("Cricket");
 */
public class SportFactory {

    // Private constructor — purely static factory
    private SportFactory() {}

    /**
     * Factory method: returns the appropriate Sport implementation.
     *
     * @param type  "Cricket", "Football", or "Badminton" (case-insensitive)
     * @return      Sport implementation
     * @throws      IllegalArgumentException for unknown types
     */
    public static Sport createSport(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Sport type must not be null.");
        }
        switch (type.trim().toLowerCase()) {
            case "cricket":   return new Cricket();
            case "football":  return new Football();
            case "badminton": return new Badminton();
            default:
                throw new IllegalArgumentException(
                    "Unknown sport type: '" + type + "'. " +
                    "Supported: Cricket, Football, Badminton.");
        }
    }

    /** Convenience: list all registered sport names. */
    public static String[] supportedSports() {
        return new String[]{"Cricket", "Football", "Badminton"};
    }
}
