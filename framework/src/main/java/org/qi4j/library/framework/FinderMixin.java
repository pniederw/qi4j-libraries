package org.qi4j.library.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.annotation.AppliesTo;
import org.qi4j.api.annotation.AppliesToFilter;
import org.qi4j.api.annotation.scope.Entity;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import static org.qi4j.api.query.QueryExpression.arg;
import static org.qi4j.api.query.QueryExpression.eq;

/**
 * Generic finder mixin.
 * <p/>
 * This mixin will be applied to all methods with names matching "findBy*".
 * The part after "findBy" will be interpreted and converted to a QueryBuilder.
 */
@AppliesTo( FinderMixin.AppliesTo.class )
public class FinderMixin
    implements InvocationHandler
{
    public static class AppliesTo
        implements AppliesToFilter
    {
        public boolean appliesTo( Method method, Class compositeType, Class mixin )
        {
            String name = method.getName();
            if( name.startsWith( "findBy" ) )
            {
                return true;
            }

            return false;
        }
    }

    // Attributes ----------------------------------------------------
    @Entity( "someQuery" ) QueryBuilderFactory factory;

    // InvocationHandler implementation ------------------------------
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        // Build query
        String methodName = method.getName();

        Class resultType = method.getReturnType();

        QueryBuilder builder = factory.newQueryBuilder( resultType );

        // Remove "findBy"
        methodName = methodName.substring( 6 );

        Object param = builder.parameter( resultType );

        Method getter = param.getClass().getMethod( "get" + methodName );

        builder = builder.where( eq( getter.invoke( param ), arg( (Comparable) args[ 0 ] ) ) );

        // Execute query
        return builder.newQuery().find();
    }

}