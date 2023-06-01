/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 */
package org.knime.credentials.base.oauth.node.generic;

import org.knime.core.webui.node.dialog.defaultdialog.widget.Label;
import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.apis.AWeberApi;
import com.github.scribejava.apis.Asana20Api;
import com.github.scribejava.apis.AutomaticAPI;
import com.github.scribejava.apis.BoxApi20;
import com.github.scribejava.apis.DataportenApi;
import com.github.scribejava.apis.DiggApi;
import com.github.scribejava.apis.DiscordApi;
import com.github.scribejava.apis.DoktornaraboteApi;
import com.github.scribejava.apis.DropboxApi;
import com.github.scribejava.apis.EtsyApi;
import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.apis.FitbitApi20;
import com.github.scribejava.apis.FlickrApi;
import com.github.scribejava.apis.Foursquare2Api;
import com.github.scribejava.apis.FoursquareApi;
import com.github.scribejava.apis.FreelancerApi;
import com.github.scribejava.apis.GeniusApi;
import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.apis.HHApi;
import com.github.scribejava.apis.HiOrgServerApi20;
import com.github.scribejava.apis.ImgurApi;
import com.github.scribejava.apis.InstagramApi;
import com.github.scribejava.apis.KaixinApi20;
import com.github.scribejava.apis.KakaoApi;
import com.github.scribejava.apis.KeycloakApi;
import com.github.scribejava.apis.LinkedInApi;
import com.github.scribejava.apis.LinkedInApi20;
import com.github.scribejava.apis.LiveApi;
import com.github.scribejava.apis.MailruApi;
import com.github.scribejava.apis.MediaWikiApi;
import com.github.scribejava.apis.MeetupApi;
import com.github.scribejava.apis.MeetupApi20;
import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.apis.MicrosoftAzureActiveDirectoryApi;
import com.github.scribejava.apis.MisfitApi;
import com.github.scribejava.apis.NaverApi;
import com.github.scribejava.apis.OdnoklassnikiApi;
import com.github.scribejava.apis.PinterestApi;
import com.github.scribejava.apis.PolarAPI;
import com.github.scribejava.apis.Px500Api;
import com.github.scribejava.apis.RenrenApi;
import com.github.scribejava.apis.SalesforceApi;
import com.github.scribejava.apis.SinaWeiboApi;
import com.github.scribejava.apis.SinaWeiboApi20;
import com.github.scribejava.apis.SkyrockApi;
import com.github.scribejava.apis.SlackApi;
import com.github.scribejava.apis.StackExchangeApi;
import com.github.scribejava.apis.TheThingsNetworkV1StagingApi;
import com.github.scribejava.apis.TheThingsNetworkV2PreviewApi;
import com.github.scribejava.apis.TrelloApi;
import com.github.scribejava.apis.TumblrApi;
import com.github.scribejava.apis.TutByApi;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.apis.UcozApi;
import com.github.scribejava.apis.ViadeoApi;
import com.github.scribejava.apis.VkontakteApi;
import com.github.scribejava.apis.WunderlistAPI;
import com.github.scribejava.apis.XeroApi20;
import com.github.scribejava.apis.XingApi;
import com.github.scribejava.apis.YahooApi;
import com.github.scribejava.apis.YahooApi20;


/**
 * Enum describing scribe-java services. Autogenerated by
 * GenerateStandardServices class.
 */
@SuppressWarnings("restriction")
public enum StandardService {

    /**
     * AWeberApi service.
     */
    @Label("AWeber")
    AWEBERAPI(AWeberApi.instance()),
    /**
     * Asana20Api service.
     */
    @Label("Asana (OAuth 2.0)")
    ASANA20API(Asana20Api.instance()),
    /**
     * AutomaticAPI service.
     */
    @Label("Automatic")
    AUTOMATICAPI(AutomaticAPI.instance()),
    /**
     * BoxApi20 service.
     */
    @Label("Box (OAuth 2.0)")
    BOXAPI20(BoxApi20.instance()),
    /**
     * DataportenApi service.
     */
    @Label("Dataporten")
    DATAPORTENAPI(DataportenApi.instance()),
    /**
     * DiggApi service.
     */
    @Label("Digg")
    DIGGAPI(DiggApi.instance()),
    /**
     * DiscordApi service.
     */
    @Label("Discord")
    DISCORDAPI(DiscordApi.instance()),
    /**
     * DoktornaraboteApi service.
     */
    @Label("Doktornarabote")
    DOKTORNARABOTEAPI(DoktornaraboteApi.instance()),
    /**
     * DropboxApi service.
     */
    @Label("Dropbox")
    DROPBOXAPI(DropboxApi.instance()),
    /**
     * EtsyApi service.
     */
    @Label("Etsy")
    ETSYAPI(EtsyApi.instance()),
    /**
     * FacebookApi service.
     */
    @Label("Facebook")
    FACEBOOKAPI(FacebookApi.instance()),
    /**
     * FitbitApi20 service.
     */
    @Label("Fitbit (OAuth 2.0)")
    FITBITAPI20(FitbitApi20.instance()),
    /**
     * FlickrApi service.
     */
    @Label("Flickr")
    FLICKRAPI(FlickrApi.instance()),
    /**
     * Foursquare2Api service.
     */
    @Label("Foursquare (OAuth 2.0)")
    FOURSQUARE2API(Foursquare2Api.instance()),
    /**
     * FoursquareApi service.
     */
    @Label("Foursquare")
    FOURSQUAREAPI(FoursquareApi.instance()),
    /**
     * FreelancerApi service.
     */
    @Label("Freelancer")
    FREELANCERAPI(FreelancerApi.instance()),
    /**
     * GeniusApi service.
     */
    @Label("Genius")
    GENIUSAPI(GeniusApi.instance()),
    /**
     * GitHubApi service.
     */
    @Label("GitHub")
    GITHUBAPI(GitHubApi.instance()),
    /**
     * GoogleApi20 service.
     */
    @Label("Google (OAuth 2.0)")
    GOOGLEAPI20(GoogleApi20.instance()),
    /**
     * HHApi service.
     */
    @Label("HH")
    HHAPI(HHApi.instance()),
    /**
     * HiOrgServerApi20 service.
     */
    @Label("HiOrg-Server")
    HIORGSERVERAPI20(HiOrgServerApi20.instance()),
    /**
     * ImgurApi service.
     */
    @Label("Imgur")
    IMGURAPI(ImgurApi.instance()),
    /**
     * InstagramApi service.
     */
    @Label("Instagram")
    INSTAGRAMAPI(InstagramApi.instance()),
    /**
     * KaixinApi20 service.
     */
    @Label("Kaixin (OAuth 2.0)")
    KAIXINAPI20(KaixinApi20.instance()),
    /**
     * KakaoApi service.
     */
    @Label("Kakao")
    KAKAOAPI(KakaoApi.instance()),
    /**
     * KeycloakApi service.
     */
    @Label("Keycloak")
    KEYCLOAKAPI(KeycloakApi.instance()),
    /**
     * LinkedInApi service.
     */
    @Label("LinkedIn")
    LINKEDINAPI(LinkedInApi.instance()),
    /**
     * LinkedInApi20 service.
     */
    @Label("LinkedIn (OAuth 2.0)")
    LINKEDINAPI20(LinkedInApi20.instance()),
    /**
     * LiveApi service.
     */
    @Label("Microsoft Live")
    LIVEAPI(LiveApi.instance()),
    /**
     * MailruApi service.
     */
    @Label("Mailru")
    MAILRUAPI(MailruApi.instance()),
    /**
     * MediaWikiApi service.
     */
    @Label("MediaWiki")
    MEDIAWIKIAPI(MediaWikiApi.instance()),
    /**
     * MeetupApi service.
     */
    @Label("Meetup")
    MEETUPAPI(MeetupApi.instance()),
    /**
     * MeetupApi20 service.
     */
    @Label("Meetup (OAuth 2.0)")
    MEETUPAPI20(MeetupApi20.instance()),
    /**
     * MicrosoftAzureActiveDirectory20Api service.
     */
    @Label("Microsoft Azure AD (OAuth 2.0)")
    MICROSOFTAZUREACTIVEDIRECTORY20API(MicrosoftAzureActiveDirectory20Api.instance()),
    /**
     * MicrosoftAzureActiveDirectoryApi service.
     */
    @Label("Microsoft Azure AD (OAuth 1.0)")
    MICROSOFTAZUREACTIVEDIRECTORYAPI(MicrosoftAzureActiveDirectoryApi.instance()),
    /**
     * MisfitApi service.
     */
    @Label("Misfit")
    MISFITAPI(MisfitApi.instance()),
    /**
     * NaverApi service.
     */
    @Label("Naver")
    NAVERAPI(NaverApi.instance()),
    /**
     * OdnoklassnikiApi service.
     */
    @Label("Odnoklassniki")
    ODNOKLASSNIKIAPI(OdnoklassnikiApi.instance()),
    /**
     * PinterestApi service.
     */
    @Label("Pinterest")
    PINTERESTAPI(PinterestApi.instance()),
    /**
     * PolarAPI service.
     */
    @Label("Polar")
    POLARAPI(PolarAPI.instance()),
    /**
     * Px500Api service.
     */
    @Label("500px")
    PX500API(Px500Api.instance()),
    /**
     * RenrenApi service.
     */
    @Label("Renren")
    RENRENAPI(RenrenApi.instance()),
    /**
     * SalesforceApi service.
     */
    @Label("Salesforce")
    SALESFORCEAPI(SalesforceApi.instance()),
    /**
     * SinaWeiboApi service.
     */
    @Label("SinaWeibo")
    SINAWEIBOAPI(SinaWeiboApi.instance()),
    /**
     * SinaWeiboApi20 service.
     */
    @Label("SinaWeibo (OAuth 2.0)")
    SINAWEIBOAPI20(SinaWeiboApi20.instance()),
    /**
     * SkyrockApi service.
     */
    @Label("Skyrock")
    SKYROCKAPI(SkyrockApi.instance()),
    /**
     * SlackApi service.
     */
    @Label("Slack")
    SLACKAPI(SlackApi.instance()),
    /**
     * StackExchangeApi service.
     */
    @Label("StackExchange")
    STACKEXCHANGEAPI(StackExchangeApi.instance()),
    /**
     * TheThingsNetworkV1StagingApi service.
     */
    @Label("The Things Network (v1-staging)")
    THETHINGSNETWORKV1STAGINGAPI(TheThingsNetworkV1StagingApi.instance()),
    /**
     * TheThingsNetworkV2PreviewApi service.
     */
    @Label("The Things Network (v2-preview)")
    THETHINGSNETWORKV2PREVIEWAPI(TheThingsNetworkV2PreviewApi.instance()),
    /**
     * TrelloApi service.
     */
    @Label("Trello")
    TRELLOAPI(TrelloApi.instance()),
    /**
     * TumblrApi service.
     */
    @Label("Tumblr")
    TUMBLRAPI(TumblrApi.instance()),
    /**
     * TutByApi service.
     */
    @Label("TutBy")
    TUTBYAPI(TutByApi.instance()),
    /**
     * TwitterApi service.
     */
    @Label("Twitter")
    TWITTERAPI(TwitterApi.instance()),
    /**
     * UcozApi service.
     */
    @Label("Ucoz")
    UCOZAPI(UcozApi.instance()),
    /**
     * ViadeoApi service.
     */
    @Label("Viadeo")
    VIADEOAPI(ViadeoApi.instance()),
    /**
     * VkontakteApi service.
     */
    @Label("Vkontakte")
    VKONTAKTEAPI(VkontakteApi.instance()),
    /**
     * WunderlistAPI service.
     */
    @Label("Wunderlist")
    WUNDERLISTAPI(WunderlistAPI.instance()),
    /**
     * XeroApi20 service.
     */
    @Label("Xero (OAuth 2.0)")
    XEROAPI20(XeroApi20.instance()),
    /**
     * XingApi service.
     */
    @Label("Xing")
    XINGAPI(XingApi.instance()),
    /**
     * YahooApi service.
     */
    @Label("Yahoo")
    YAHOOAPI(YahooApi.instance()),
    /**
     * YahooApi20 service.
     */
    @Label("Yahoo (OAuth 2.0)")
    YAHOOAPI20(YahooApi20.instance());


    private DefaultApi10a m_api10;
    private DefaultApi20 m_api20;

    private StandardService(final DefaultApi10a api) {
        m_api10 = api;
    }

    private StandardService(final DefaultApi20 api) {
        m_api20 = api;
    }

    /**
     * @return the {@link DefaultApi10a} instance.
     */
    public DefaultApi10a getApi10() {
        return m_api10;
    }

    /**
     * @return the {@link DefaultApi20} instance.
     */
    public DefaultApi20 getApi20() {
        return m_api20;
    }

    /**
     * @return Whether the service is OAuth 1.0 or OAuth 2.0
     */
    public boolean isApi20() {
        return m_api20 != null;
    }
}
