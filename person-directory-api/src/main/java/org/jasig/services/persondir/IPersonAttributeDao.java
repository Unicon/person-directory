/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data access object which, for a given {@link Map} of query
 * data, returns a {@link Map} from attribute names to attribute
 * values.
 * 
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist
 * @version $Revision$ $Date$
 */
public interface IPersonAttributeDao {

    /**
     * Obtains a mutable {@link Map} from attribute names to values for
     * the given query seed which is an immutable Map. The values may be mutable objects but it is
     * recommended that they be immutable.<br>
     * 
     * This method returns according to the following rules:<br>
     * <ul>
     *  <li>If the user exists and has attributes a populated {@link Map} is returned.</li>
     *  <li>If the user exists and has no attributes an empty {@link Map} is returned.</li>
     *  <li>If the user doesn't exist <code>null</code> is returned.</li>
     *  <li>If an error occurs while getting the attributes the appropriate exception will be propagated.</li>
     * </ul>
     * <br>
     * Unless otherwise specified by an implementation the returned {@link Map}
     * will not be a union of the seed and query results. If your are given a
     * {@link Map} that includes the attribute "phone" and value "555-1212" and
     * the returned {@link Map} contains the attribute "phone" with the value
     * "555-1212", this means that your implementation also believes that the
     * "phone" attribute should have this value.
     * 
     * @param seed immutable Map of attributes to values to seed the query
     * @return Map from attribute names to values
     * @throws IllegalArgumentException If <code>seed</code> is <code>null.</code>
     */
    public Map<String, List<Object>> getMultivaluedUserAttributes(final Map<String, List<Object>> seed);

    /**
     * This method uses a single attribute to get a {@link Map} of user
     * attributes. 
     * <br>
     * This methods follows the same return rules as {@link #getUserAttributes(Map)}
     * 
     * @param uid The string to use as the value in the seed
     * @return Map from attribute names to values
     * @see #getUserAttributes(Map)
     */
    public Map<String, List<Object>> getMultivaluedUserAttributes(final String uid);
    
    /**
     * This method returns single-valued user attributes. This method has the same
     * behavior as {@link #getMultivaluedUserAttributes(Map)} other than the single-valued
     * return Map
     * 
     * @see #getMultivaluedUserAttributes(Map)
     */
    public Map<String, Object> getUserAttributes(final Map<String, Object> seed);
    
    /**
     * This method returns single-valued user attributes. This method has the same
     * behavior as {@link #getMultivaluedUserAttributes(String)} other than the single-valued
     * return Map
     * 
     * @see #getMultivaluedUserAttributes(String)
     */
    public Map<String, Object> getUserAttributes(final String uid);

    /**
     * Gets a {@link Set} of attribute names that may be returned by the
     * {@link #getUserAttributes(Map)}. The names returned represent all
     * possible names {@link #getUserAttributes(Map)} could return. If the
     * dao doesn't have a way to know all possible attribute names this
     * method should return <code>null</code>.
     * <br>
     * Returns an immutable {@link Set}.
     * 
     * @return A {@link Set} of possible attribute names for user queries.
     */
    public Set<String> getPossibleUserAttributeNames();
}