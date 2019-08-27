/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Address within a Payment Request.
 */
public class PaymentAddress {
    /**
     * This is the CLDR (Common Locale Data Repository) region code. For
     * example, US, GB, CN, or JP.
     */
    @JsonProperty(value = "country")
    private String country;

    /**
     * This is the most specific part of the address. It can include, for
     * example, a street name, a house number, apartment number, a rural
     * delivery route, descriptive instructions, or a post office box number.
     */
    @JsonProperty(value = "addressLine")
    private List<String> addressLine;

    /**
     * This is the top level administrative subdivision of the country. For
     * example, this can be a state, a province, an oblast, or a prefecture.
     */
    @JsonProperty(value = "region")
    private String region;

    /**
     * This is the city/town portion of the address.
     */
    @JsonProperty(value = "city")
    private String city;

    /**
     * This is the dependent locality or sublocality within a city. For
     * example, used for neighborhoods, boroughs, districts, or UK dependent
     * localities.
     */
    @JsonProperty(value = "dependentLocality")
    private String dependentLocality;

    /**
     * This is the postal code or ZIP code, also known as PIN code in India.
     */
    @JsonProperty(value = "postalCode")
    private String postalCode;

    /**
     * This is the sorting code as used in, for example, France.
     */
    @JsonProperty(value = "sortingCode")
    private String sortingCode;

    /**
     * This is the BCP-47 language code for the address. It's used to determine
     * the field separators and the order of fields when formatting the address
     * for display.
     */
    @JsonProperty(value = "languageCode")
    private String languageCode;

    /**
     * This is the organization, firm, company, or institution at this address.
     */
    @JsonProperty(value = "organization")
    private String organization;

    /**
     * This is the name of the recipient or contact person.
     */
    @JsonProperty(value = "recipient")
    private String recipient;

    /**
     * This is the phone number of the recipient or contact person.
     */
    @JsonProperty(value = "phone")
    private String phone;

    /**
     * Get the country value.
     *
     * @return the country value
     */
    public String getCountry() {
        return this.country;
    }

    /**
     * Set the country value.
     *
     * @param withCountry the country value to set
     */
    public void setCountry(String withCountry) {
        this.country = withCountry;
    }

    /**
     * Get the addressLine value.
     *
     * @return the addressLine value
     */
    public List<String> getAddressLine() {
        return this.addressLine;
    }

    /**
     * Set the addressLine value.
     *
     * @param withAddressLine the addressLine value to set
     */
    public void setAddressLine(List<String> withAddressLine) {
        this.addressLine = withAddressLine;
    }

    /**
     * Get the region value.
     *
     * @return the region value
     */
    public String getRegion() {
        return this.region;
    }

    /**
     * Set the region value.
     *
     * @param withRegion the region value to set
     */
    public void setRegion(String withRegion) {
        this.region = withRegion;
    }

    /**
     * Get the city value.
     *
     * @return the city value
     */
    public String getCity() {
        return this.city;
    }

    /**
     * Set the city value.
     *
     * @param withCity the city value to set
     */
    public void setCity(String withCity) {
        this.city = withCity;
    }

    /**
     * Get the dependentLocality value.
     *
     * @return the dependentLocality value
     */
    public String getDependentLocality() {
        return this.dependentLocality;
    }

    /**
     * Set the dependentLocality value.
     *
     * @param withDependentLocality the dependentLocality value to set
     * @return the PaymentAddress object itself.
     */
    public void setDependentLocality(String withDependentLocality) {
        this.dependentLocality = withDependentLocality;
    }

    /**
     * Get the postalCode value.
     *
     * @return the postalCode value
     */
    public String postalCode() {
        return this.postalCode;
    }

    /**
     * Set the postalCode value.
     *
     * @param withPostalCode the postalCode value to set
     * @return the PaymentAddress object itself.
     */
    public void setPostalCode(String withPostalCode) {
        this.postalCode = withPostalCode;
    }

    /**
     * Get the sortingCode value.
     *
     * @return the sortingCode value
     */
    public String getSortingCode() {
        return this.sortingCode;
    }

    /**
     * Set the sortingCode value.
     *
     * @param withSortingCode the sortingCode value to set
     */
    public void setSortingCode(String withSortingCode) {
        this.sortingCode = withSortingCode;
    }

    /**
     * Get the languageCode value.
     *
     * @return the languageCode value
     */
    public String getLanguageCode() {
        return this.languageCode;
    }

    /**
     * Set the languageCode value.
     *
     * @param withLanguageCode the languageCode value to set
     * @return the PaymentAddress object itself.
     */
    public void setLanguageCode(String withLanguageCode) {
        this.languageCode = withLanguageCode;
    }

    /**
     * Get the organization value.
     *
     * @return the organization value
     */
    public String getOrganization() {
        return this.organization;
    }

    /**
     * Set the organization value.
     *
     * @param withOrganization the organization value to set
     */
    public void setOrganization(String withOrganization) {
        this.organization = withOrganization;
    }

    /**
     * Get the recipient value.
     *
     * @return the recipient value
     */
    public String getRecipient() {
        return this.recipient;
    }

    /**
     * Set the recipient value.
     *
     * @param withRecipient the recipient value to set
     */
    public void setRecipient(String withRecipient) {
        this.recipient = withRecipient;
    }

    /**
     * Get the phone value.
     *
     * @return the phone value
     */
    public String getPhone() {
        return this.phone;
    }

    /**
     * Set the phone value.
     *
     * @param withPhone the phone value to set
     */
    public void setPhone(String withPhone) {
        this.phone = withPhone;
    }
}
