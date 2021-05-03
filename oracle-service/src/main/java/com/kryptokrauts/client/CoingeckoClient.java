package com.kryptokrauts.client;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

@Path("/")
@RegisterRestClient
public interface CoingeckoClient {

  @GET
  @Path("/{coin}/history")
  @Produces(MediaType.APPLICATION_JSON)
  public String getCoinValue(@PathParam("coin") String coin, @QueryParam("date") String date,
      @QueryParam("localization") String localization);

}
