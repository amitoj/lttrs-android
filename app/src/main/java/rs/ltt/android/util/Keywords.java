package rs.ltt.android.util;

import java.util.Collection;

import rs.ltt.jmap.common.entity.IdentifiableEmailWithKeywords;


public class Keywords {

    public static boolean anyHas(Collection<?extends IdentifiableEmailWithKeywords> emails, String keyword) {
        for(IdentifiableEmailWithKeywords email : emails) {
            if (email.getKeywords().keySet().contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static boolean everyHas(Collection<?extends IdentifiableEmailWithKeywords> emails, String keyword) {
        for(IdentifiableEmailWithKeywords email : emails) {
            if (!email.getKeywords().keySet().contains(keyword)) {
                return false;
            }
        }
        return true;
    }
}
