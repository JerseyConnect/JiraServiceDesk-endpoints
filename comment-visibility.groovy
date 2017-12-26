import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.bc.user.search.UserSearchService
import com.atlassian.sal.api.user.UserManager
import com.atlassian.jira.entity.property.JsonEntityPropertyManager
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import com.atlassian.jira.user.ApplicationUser;
 
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.BaseScript
 
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response
 
@BaseScript CustomEndpointDelegate delegate
 
markCommentPrivate(httpMethod: "POST", groups: ["jira-administrators"])
{ MultivaluedMap queryParams, String body, HttpServletRequest request ->
 
  def ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
  def jsonManager = ComponentAccessor.getComponent(JsonEntityPropertyManager)
   
  def json = new JsonSlurper().parseText(body)
  def propertyCount = 0
  def commentList = new ArrayList<>()
  json.each {
    aComment ->
      def commentId = Integer.parseInt( aComment.id.toString() )
      jsonManager.put(user, "sd.comment.property", commentId, "sd.public.comment", "{ \"internal\" : true}" , (java.util.function.BiFunction) null, false)
      propertyCount++;
  }
 
  def response = new ArrayList<>()
  response.add( new JsonSlurper().parseText('{ "comments_updated": ' + propertyCount + ' }') )
 
  return Response.ok( new JsonBuilder(response).toPrettyString() ).build();
}
