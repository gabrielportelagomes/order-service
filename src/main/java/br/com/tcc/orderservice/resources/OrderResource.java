package br.com.tcc.orderservice.resources;

import br.com.tcc.orderservice.dtos.OrderRequestDTO;
import br.com.tcc.orderservice.models.Order;
import br.com.tcc.orderservice.services.OrderService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    OrderService service;

    @POST
    @Path("/sync")
    public Response createOrderSync(OrderRequestDTO dto) {
        Order createdOrder = service.processOrderSync(dto);

        return Response.ok(createdOrder).build();
    }
}