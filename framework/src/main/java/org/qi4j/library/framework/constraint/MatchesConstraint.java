package org.qi4j.library.framework.constraint;

import java.util.regex.Pattern;
import org.qi4j.ParameterConstraint;
import org.qi4j.library.framework.constraint.annotation.Matches;

/**
 * TODO
 */
public class MatchesConstraint
    implements ParameterConstraint<Matches, String>
{
    public boolean isValid( Matches annotation, String argument )
    {
        if( argument != null )
        {
            Pattern pattern = Pattern.compile( annotation.value() );
            return pattern.matcher( argument ).matches();
        }

        return false;
    }
}