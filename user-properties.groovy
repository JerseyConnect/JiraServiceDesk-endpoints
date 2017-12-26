import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.bc.user.search.UserSearchService
import com.atlassian.sal.api.user.UserManager
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
 
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.BaseScript
 
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response
 
@BaseScript CustomEndpointDelegate delegate
 
setUserProperties(httpMethod: "POST", groups: ["jira-administrators"])
{ MultivaluedMap queryParams, String body, HttpServletRequest request ->
 
  def userPropertyManager = ComponentAccessor.getUserPropertyManager()
  def userManager = ComponentAccessor.getUserManager()
  def userSearchService = ComponentAccessor.getComponent(UserSearchService.class)
 
  def json = new JsonSlurper().parseText(body)
  def propertyCount = 0
  def customerList = new ArrayList<>()
  json.each {
    aCustomer ->
      def user = userManager.getUserByName(aCustomer.name.toString())
      aCustomer.properties.each {
        aProperty ->
          userPropertyManager.getPropertySet(user).setString("jira.meta." + aProperty.key.toString(), aProperty.value.toString())
          propertyCount++
      }
      customerList.add(user)
  }
 
  def response = new ArrayList<>()
  response.add( new JsonSlurper().parseText('{ "properties_changed": ' + propertyCount + ' }') )
  response.add( customerList )
 
  return Response.ok( new JsonBuilder(response).toPrettyString() ).build();
}
