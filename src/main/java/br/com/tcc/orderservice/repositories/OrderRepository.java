package br.com.tcc.orderservice.repositories;

import br.com.tcc.orderservice.models.Order;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order> {}
