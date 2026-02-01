package br.com.tcc.orderservice.clients;

import br.com.tcc.orderservice.dtos.InventoryRequestDTO;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "inventory-api")
@Path("/inventory")
public interface InventoryClient {

    @POST
    @Path("/reserve")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    void reserveStock(InventoryRequestDTO inventoryDto);

    @POST
    @Path("/cancel") // O path do método
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    void cancelReservation(InventoryRequestDTO inventoryDto);
}
