/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.index.schema;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.neo4j.values.Value;
import org.neo4j.values.Values;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith( Parameterized.class )
public class RawBitsTest
{
    @Parameterized.Parameter()
    public String name;

    @Parameterized.Parameter( 1 )
    public NumberLayout layout;

    @Parameterized.Parameters( name = "{0}" )
    public static List<Object[]> layouts()
    {
        return asList(
                new Object[]{"Unique",
                        new UniqueNumberLayout()
                },
                new Object[]{"NonUnique",
                        new NonUniqueNumberLayout()
                }
        );
    }

    final List<Object> objects = Arrays.asList(
            Double.NEGATIVE_INFINITY,
            -Double.MAX_VALUE,
            Long.MIN_VALUE,
            Long.MIN_VALUE + 1,
            Integer.MIN_VALUE,
            Short.MIN_VALUE,
            Byte.MIN_VALUE,
            0,
            Double.MIN_VALUE,
            Double.MIN_NORMAL,
            Float.MIN_VALUE,
            Float.MIN_NORMAL,
            1L,
            1.1d,
            1.2f,
            Math.E,
            Math.PI,
            (byte) 10,
            (short) 20,
            Byte.MAX_VALUE,
            Short.MAX_VALUE,
            Integer.MAX_VALUE,
            9007199254740992D,
            9007199254740993L,
            Long.MAX_VALUE,
            Float.MAX_VALUE,
            Double.MAX_VALUE,
            Double.POSITIVE_INFINITY,
            Double.NaN
    );

    @Test
    public void mustSortInSameOrderAsValueComparator() throws Exception
    {
        // given
        List<Value> values = asValueObjects( objects );
        List<SchemaNumberKey> schemaNumberKeys = asSchemaNumberKeys( values );
        Collections.shuffle( values );
        Collections.shuffle( schemaNumberKeys );

        // when
        values.sort( Values.COMPARATOR );
        schemaNumberKeys.sort( layout );
        List<Value> actual = schemaNumberKeys.stream()
                .map( k -> RawBits.asNumberValue( k.rawValueBits, k.type ) )
                .collect( Collectors.toList() );

        // then
        assertSameOrder( actual, values );
    }

    private void assertSameOrder( List<Value> actual, List<Value> values )
    {
        assertEquals( actual.size(), values.size() );
        for ( int i = 0; i < actual.size(); i++ )
        {
            Number actualAsNumber = (Number) actual.get( i ).asObject();
            Number valueAsNumber = (Number) values.get( i ).asObject();
            //noinspection StatementWithEmptyBody
            if ( Double.isNaN( actualAsNumber.doubleValue() ) && Double.isNaN( valueAsNumber.doubleValue() ) )
            {
                // Don't compare equals because NaN does not equal itself
            }
            else
            {
                assertEquals( actual.get( i ), values.get( i ) );
            }
        }
    }

    private List<Value> asValueObjects( List<Object> objects )
    {
        List<Value> values = new ArrayList<>();
        for ( Object object : objects )
        {
            values.add( Values.of( object ) );
        }
        return values;
    }

    private List<SchemaNumberKey> asSchemaNumberKeys( List<Value> values )
    {
        List<SchemaNumberKey> schemaNumberKeys = new ArrayList<>();
        for ( Value value : values )
        {
            SchemaNumberKey key = new SchemaNumberKey();
            key.from( 0, value );
            schemaNumberKeys.add( key );
        }
        return schemaNumberKeys;
    }
}
