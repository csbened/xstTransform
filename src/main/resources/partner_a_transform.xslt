<?xml version="1.0"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ota="http://www.opentravel.org/OTA/2003/05"
                xmlns:test="xalan://com.roomex.xmltransform.XsltFunctions" >
    <xsl:output version="1.0" standalone="yes" omit-xml-declaration="yes" indent="yes" encoding="UTF-8" />
    <xsl:template match="/">
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:oas1="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
                          xmlns:oas="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"
                          xmlns:add="http://www.w3.org/2005/08/addressing">
            <soapenv:Header>
                <add:MessageID><xsl:value-of select="test:getUniqueId()"/></add:MessageID><!-- Random UUID as $MessageID -->
                <add:Action><xsl:value-of select="//ota:RequestorOption[@Name='action']/@Value" /></add:Action><!--Hotel_MultiSingleAvailability_10-->
                <add:To>https://<xsl:value-of select="//ota:RequestorOption[@Name='targetHost']/@Value" />/<xsl:value-of select="//ota:RequestorOption[@Name='endpoint']/@Value" /></add:To><!--https://nodeD1.test.webservices.amadeus.com/1ASIWROORVZU-->
                <oas:Security>
                    <oas:UsernameToken oas1:Id="UsernameToken-1">
                        <xsl:variable name="messageId" select="//ota:RequestorID/@ID" />
                        <xsl:variable name="created" select="test:getCurrentDateTime()" />
                        <oas:Username><xsl:value-of select="$messageId" /></oas:Username><!--WSRMXRDOO-->
                        <oas:Nonce
 EncodingType="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary"><xsl:value-of
                                select="test:getBase64($messageId)" /></oas:Nonce><!-- Nonce is Base64($MessageID)-->
                        <!-- $ClearPassword is taken from ota:RequestorID/@MessagePassword -->
                        <oas:Password
 Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest"><xsl:value-of
                                select="test:getPassword($messageId,$created, //ota:RequestorID/MessagePassword )" /></oas:Password>
                        <!-- Password is Base64(Sha1($Nonce + $Created + Sha1($ClearPassword))) -->
                        <oas1:Created><xsl:value-of select="$created" /></oas1:Created><!-- Current datetime as $Created -->
                    </oas:UsernameToken>
                </oas:Security>
            </soapenv:Header>
            <soapenv:Body>
                <availabilityRequest ><xsl:attribute name="inputCurrency">
                        <xsl:value-of select="//ota:OTA_HotelAvailRQ/@RequestedCurrency" />
                    </xsl:attribute><xsl:attribute name="inputLanguageCode">
                        <xsl:value-of select="test:upperCase(//ota:OTA_HotelAvailRQ/@PrimaryLangID)" />
                    </xsl:attribute>
                    <globalInputParameters>
                        <hotelInputDetails ><xsl:attribute name="chainCode">
                                <xsl:value-of select="test:getChainCode(//ota:HotelRef/@HotelCode)" />
                            </xsl:attribute><xsl:attribute name="hotelId">
                                <xsl:value-of select="test:getHotelCode(//ota:HotelRef/@HotelCode)" />
                            </xsl:attribute><xsl:attribute name="locationCode">
                                <xsl:value-of select="//ota:HotelRef/@HotelCityCode" />
                            </xsl:attribute></hotelInputDetails>
                        <hotelStayDuration ><xsl:attribute name="numberOfNights">
                                <xsl:value-of select="test:getNumberOfNights(//ota:StayDateRange/@Start, //ota:StayDateRange/@End)" />
                            </xsl:attribute><xsl:attribute name="startDate">
                                <xsl:value-of select="test:getStartDate(//ota:StayDateRange/@Start)" />
                            </xsl:attribute></hotelStayDuration>
                    </globalInputParameters>
                    <roomInputParameters>
                        <xsl:variable name="roomGroups" select="test:getRoomGroups(//ota:RoomStayCandidate)" as="java.util.Map" />
                        <rooms ><xsl:attribute name="uniqueRoomGroups">
                            <xsl:value-of select="test:getSize($roomGroups)" />
                        </xsl:attribute><xsl:attribute name="numberOfChildren">
                            <xsl:value-of select="test:getNumberOfChildren($roomGroups)" />
                        </xsl:attribute>
                            <xsl:for-each select="test:getList($roomGroups)">
                                <room><xsl:attribute name="numberOfRooms">
                                    <xsl:value-of select="numberOfRooms" />
                                </xsl:attribute><xsl:attribute name="numberOfAdults">
                                    <xsl:value-of select="numberOfAdults" />
                                </xsl:attribute></room>
                            </xsl:for-each>
                        </rooms>
                    </roomInputParameters>
                </availabilityRequest>
            </soapenv:Body>
        </soapenv:Envelope>
    </xsl:template>
</xsl:stylesheet>