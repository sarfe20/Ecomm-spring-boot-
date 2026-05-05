package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Platform;
import com.ecommerce.project.model.Price;
import com.ecommerce.project.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {
    List<Price> findByProductProductIdOrderByDateAscIdAsc(Long productId);

    Optional<Price> findFirstByProductProductIdOrderByPriceAsc(Long productId);

    Optional<Price> findFirstByProductProductIdOrderByPriceDesc(Long productId);

    Optional<Price> findTopByProductAndPlatformOrderByDateDescIdDesc(Product product, Platform platform);

    List<Price> findBySourceUrlIsNotNullAndSourceUrlNot(String sourceUrl);
}
