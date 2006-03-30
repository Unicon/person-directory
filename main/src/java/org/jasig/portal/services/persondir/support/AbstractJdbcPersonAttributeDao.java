/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;

/**
 * Provides basic implementations for configuring query attributes, ensuring queries have the needed
 * attributes to execute, run the query via an abstract MappingSqlQuery stub.
 * 
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 * @version $Revision$
 */
public abstract class AbstractJdbcPersonAttributeDao extends AbstractDefaultQueryPersonAttributeDao {
    
    /**
     * {@link List} of attributes to use in the query.
     */
    private final List queryAttributes;
    
    
    /***
     * Create the DAO, configured with the needed query information.
     *
     * @param ds The {@link DataSource} to run the queries against.
     * @param attrList The list of arguments for the query.
     * @param sql The SQL query to run.
     */
    public AbstractJdbcPersonAttributeDao(final DataSource ds, final List attrList, final String sql) {
        if (super.log.isTraceEnabled()) {
            log.trace("entering AbstractJdbcPersonAttributeDao(" + ds + ", " + attrList + ", " + sql + ")");
        }
        if (attrList == null)
            throw new IllegalArgumentException("attrList cannot be null");

        //Defensive copy of the query attribute list
        final List defensiveCopy = new ArrayList(attrList);
        this.queryAttributes = Collections.unmodifiableList(defensiveCopy);

        if (log.isTraceEnabled()) {
            log.trace("Constructed " + this);
        }
    }
    

    
    /**
     * Takes the {@link List} from the {@link AbstractPersonAttributeMappingQuery} implementation
     * and passes it to the implementing the class for parsing into the returned user attribute Map.
     * 
     * @param queryResults Results from the query done using the {@link AbstractPersonAttributeMappingQuery} returned by {@link #getAttributeQuery()}
     * @return The return value must follow the same rules as {@link org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(Map)} 
     */
    protected abstract Map parseAttributeMapFromResults(final List queryResults);
    
    /**
     * @return The subclasses implementation of the {@link AbstractPersonAttributeMappingQuery}.
     */
    protected abstract AbstractPersonAttributeMappingQuery getAttributeQuery();


    /***
     * Checks the seed for being null.<br>
     * Ensures the seed contains the attributes needed to run the query, returns null if they aren't available.<br>
     * Compiles the Object[] of arguments.<br>
     * Gets the query from the {@link #getAttributeQuery()} method.<br>
     * Runs the query.<br>
     * Calls {@link #parseAttributeMapFromResults(List)} with the query results.<br>
     * Returns results from {@link #parseAttributeMapFromResults(List)} link.<br>
     *
     * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public final Map getUserAttributes(final Map seed) {
        if (seed == null)
            throw new IllegalArgumentException("The query seed Map cannot be null.");

        //Ensure the data needed to run the query is avalable
        if (!((this.queryAttributes != null && seed.keySet().containsAll(this.queryAttributes)) ||
              (this.queryAttributes == null && seed.containsKey(this.getDefaultAttributeName())))) {
            return null;
        }

        //Can't just to a toArray here since the order of the keys in the Map
        //may not match the order of the keys in the List and it is important to
        //the query.
        final Object[] args = new Object[this.queryAttributes.size()];

        for (int index = 0; index < args.length; index++) {
            final String attrName = (String)this.queryAttributes.get(index);
            args[index] = seed.get(attrName);
        }

        final AbstractPersonAttributeMappingQuery query = this.getAttributeQuery();
        final List queryResults = query.execute(args);
        final Map userAttributes = this.parseAttributeMapFromResults(queryResults);
        return userAttributes;
    }

    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("AbstractJdbcPersonAttributeDao ");
        sb.append("query=").append(this.getAttributeQuery());
        sb.append(" queryAttributes=").append(this.queryAttributes);
        return sb.toString();
    }
    


    /**
     * An object which will execute a SQL query with the expectation
     * of yielding a ResultSet with zero or one rows, which it maps
     * to null or to a Map from uPortal attribute names to values.
     */
    protected abstract class AbstractPersonAttributeMappingQuery extends MappingSqlQuery {
        /**
         * Instantiate the query, providing a DataSource against which the query
         * will run and the SQL representing the query, which should take exactly
         * one parameter: the unique ID of the user.
         * 
         * @param ds The data source to use for running the query against.
         * @param sql The SQL to run against the data source.
         */
        public AbstractPersonAttributeMappingQuery(final DataSource ds, final String sql) {
            super(ds, sql);
            
            //Configures the SQL parameters, everything is assumed to be VARCHAR
            for (final Iterator attrNames = queryAttributes.iterator(); attrNames.hasNext(); ) {
                final String attrName = (String)attrNames.next();
                this.declareParameter(new SqlParameter(attrName, Types.VARCHAR));
            }

            //One time compilation of the query
            this.compile();
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(this.getClass().getName());
            sb.append(" SQL=[").append(super.getSql()).append("]");
            return sb.toString();
        }
    }
}
