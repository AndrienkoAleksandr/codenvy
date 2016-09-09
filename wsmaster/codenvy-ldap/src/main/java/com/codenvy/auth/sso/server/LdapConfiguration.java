/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.auth.sso.server;

import org.ldaptive.sasl.Mechanism;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SecurityStrength;

/**
 * Created by sj on 08.09.16.
 */
public class LdapConfiguration {


    /**
     * The enum Authentication types.
     */
    public enum AuthenticationType {
        /**
         * Active Directory.
         */
        AD,
        /**
         * Authenticated Search.
         */
        AUTHENTICATED,
        /**
         * Direct Bind.
         */
        DIRECT,
        /**
         * Anonymous Search.
         */
        ANONYMOUS,
        /**
         * SASL bind search.
         */
        SASL
    }


    //    private int     minPoolSize          = 3;
//    private int     maxPoolSize          = 10;
//    private boolean validateOnCheckout   = true;
//    private boolean validatePeriodically = true;
//    private long    validatePeriod       = 300;
//
//    private boolean failFast      = true;
//    private long    idleTime      = 600;
//    private long    prunePeriod   = 10000;
//    private long    blockWaitTime = 6000;
//
//    private String  ldapUrl = "ldap://localhost:389";
//    private boolean useSsl  = true;
//    private boolean useStartTls;
//    private long connectTimeout = 5000;
//
//


    private String             dnFormat;
    private String             principalAttributePassword;
    private boolean            allowMultiplePrincipalAttributeValues;
    private AuthenticationType type;
    private boolean subtreeSearch = true;
    private String              baseDn;
    private String              userFilter;
    private String              trustCertificates;
    private String              keystore;
    private String              keystorePassword;
    private String              keystoreType;
    private int                 minPoolSize;
    private int                 maxPoolSize;
    private boolean             validateOnCheckout;
    private boolean             validatePeriodically;
    private long                validatePeriod;
    private boolean             failFast;
    private long                idleTime;
    private long                prunePeriod;
    private long                blockWaitTime;
    private String              ldapUrl;
    private boolean             useSsl;
    private boolean             useStartTls;
    private long                connectTimeout;
    private String              providerClass;
    private boolean             allowMultipleDns;
    private String              bindDn;
    private String              bindCredential;
    private String              saslRealm;
    private Mechanism           saslMechanism;
    private String              saslAuthorizationId;
    private SecurityStrength    saslSecurityStrength;
    private Boolean             saslMutualAuth;
    private QualityOfProtection saslQualityOfProtection;


    public boolean isAllowMultipleDns() {
        return allowMultipleDns;
    }

    public AuthenticationType getType() {
        return type;
    }

    public String getDnFormat() {
        return dnFormat;
    }

    public String getPrincipalAttributePassword() {
        return principalAttributePassword;
    }

    public boolean isSubtreeSearch() {
        return subtreeSearch;
    }

    public String getBaseDn() {
        return baseDn;
    }

    public String getUserFilter() {
        return userFilter;
    }

    public String getTrustCertificates() {
        return trustCertificates;
    }

    public String getKeystore() {
        return keystore;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public String getKeystoreType() {
        return keystoreType;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public boolean isValidateOnCheckout() {
        return validateOnCheckout;
    }

    public boolean isValidatePeriodically() {
        return validatePeriodically;
    }

    public long getValidatePeriod() {
        return validatePeriod;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public long getIdleTime() {
        return idleTime;
    }

    public long getPrunePeriod() {
        return prunePeriod;
    }

    public long getBlockWaitTime() {
        return blockWaitTime;
    }

    public String getLdapUrl() {
        return ldapUrl;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public boolean isUseStartTls() {
        return useStartTls;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public String getProviderClass() {
        return providerClass;
    }

    public String getBindDn() {
        return bindDn;
    }

    public String getBindCredential() {
        return bindCredential;
    }

    public String getSaslRealm() {
        return saslRealm;
    }

    public Mechanism getSaslMechanism() {
        return saslMechanism;
    }

    public String getSaslAuthorizationId() {
        return saslAuthorizationId;
    }

    public SecurityStrength getSaslSecurityStrength() {
        return saslSecurityStrength;
    }

    public Boolean getSaslMutualAuth() {
        return saslMutualAuth;
    }

    public QualityOfProtection getSaslQualityOfProtection() {
        return saslQualityOfProtection;
    }
}
