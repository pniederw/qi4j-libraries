/*
 * Copyright (c) 2010 Paul Merlin <paul@nosphere.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.qi4j.library.shiro.domain.x509;

import java.io.IOException;
import java.io.StringReader;
import java.security.cert.X509Certificate;

import org.apache.shiro.crypto.CryptoException;
import org.bouncycastle.openssl.PEMReader;

import org.joda.time.DateTime;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

@Mixins( X509.Mixin.class )
public interface X509
        extends X509Light
{

    Property<DateTime> issuanceDateTime();

    Property<DateTime> expirationDateTime();

    Property<String> pem();

    X509Certificate x509Certificate();

    abstract class Mixin
            implements X509
    {

        @This
        private X509 state;

        public X509Certificate x509Certificate()
        {
            try {
                return ( X509Certificate ) new PEMReader( new StringReader( state.pem().get() ) ).readObject();
            } catch ( IOException ex ) {
                throw new CryptoException( "Unable to read pem", ex );
            }
        }

    }

}
