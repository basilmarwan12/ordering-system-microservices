package com.ordering.product.repository;

import com.ordering.product.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Pessimistic write lock so two concurrent order-created events can't both
     * read the same stock count and both decrement it past zero.
     * Fine at this scale; if throughput becomes an issue, move to an optimistic
     * @Version column + retry instead.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids")
    List<Product> findAllForUpdate(List<Long> ids);

    Optional<Product> findByName(String name);
}
