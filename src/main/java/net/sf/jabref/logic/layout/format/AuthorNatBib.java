package net.sf.jabref.logic.layout.format;

import net.sf.jabref.logic.layout.LayoutFormatter;
import net.sf.jabref.model.entry.AuthorList;

/**
 * Natbib style: Last names only. Two authors are separated by "and",
 * three or more authors are given as "Smith et al."
 *
 * @author Morten O. Alver
 */
public class AuthorNatBib implements LayoutFormatter {

    @Override
    public String format(String fieldText) {
        return AuthorList.fixAuthorNatbib(fieldText);
    }
}
