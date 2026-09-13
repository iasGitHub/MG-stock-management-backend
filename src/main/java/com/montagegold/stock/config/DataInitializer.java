package com.montagegold.stock.config;

import com.montagegold.stock.entity.Category;
import com.montagegold.stock.entity.StockMovement;
import com.montagegold.stock.entity.Supplier;
import com.montagegold.stock.entity.Product;
import com.montagegold.stock.entity.User;
import com.montagegold.stock.enums.MovementType;
import com.montagegold.stock.enums.Role;
import com.montagegold.stock.repository.CategoryRepository;
import com.montagegold.stock.repository.StockMovementRepository;
import com.montagegold.stock.repository.SupplierRepository;
import com.montagegold.stock.repository.ProductRepository;
import com.montagegold.stock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PasswordEncoder passwordEncoder;

    private Category category(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> categoryRepository.save(Category.builder().name(name).build()));
    }

    private void seedProduct(User user, Category category, String reference, String name,
                             String description, int initialQuantity, int minThreshold,
                             double unitPrice) {
        Product product = productRepository.save(Product.builder()
                .reference(reference).name(name).description(description)
                .category(category).stockQuantity(0)
                .minThreshold(minThreshold).unitPrice(unitPrice)
                .build());
        if (initialQuantity > 0) {
            stockMovementRepository.save(StockMovement.builder()
                    .product(product)
                    .type(MovementType.IN)
                    .quantity(initialQuantity)
                    .reason("Inventaire initial")
                    .unitPrice(product.getUnitPrice())
                    .user(user)
                    .build());
        }
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Administrator")
                    .role(Role.ADMIN)
                    .build());

            userRepository.save(User.builder()
                    .username("manager")
                    .password(passwordEncoder.encode("manager123"))
                    .fullName("Stock Manager")
                    .role(Role.MANAGEMENT)
                    .build());

            log.info("Default accounts created: admin/admin123, manager/manager123");
        }

        if (productRepository.count() == 0) {
            userRepository.findByUsername("admin").ifPresent(admin -> {
                seedProduct(admin, category("IT"), "REF-001", "HP Portable Laptop",
                        "HP ProBook 15 inches - i5 8GB RAM", 15, 5, 450000.0);
                seedProduct(admin, category("IT"), "REF-002", "Canon Printer",
                        "Mono laser printer", 3, 5, 180000.0);
                seedProduct(admin, category("Stationery"), "REF-003", "A4 Paper Ream",
                        "Office paper 80g - pack of 500 sheets", 120, 30, 3500.0);
                seedProduct(admin, category("Consumables"), "REF-004", "Black Toner",
                        "Compatible toner cartridge for Canon", 8, 10, 25000.0);
            });
            log.info("Sample products created");
        }

        if (supplierRepository.count() == 0) {
            supplierRepository.save(Supplier.builder()
                    .nif("FRS-001").name("TechnoImport SARL")
                    .phone("+222 45 25 12 34")
                    .address("Industrial zone, Nouakchott").build());

            supplierRepository.save(Supplier.builder()
                    .nif("FRS-002").name("Bureau Plus")
                    .phone("+222 45 29 87 65")
                    .address("Avenue Charles de Gaulle, Nouakchott").build());

            supplierRepository.save(Supplier.builder()
                    .nif("FRS-003").name("Mauritania Supplies")
                    .phone("+222 46 33 21 09").build());

            log.info("Sample suppliers created");
        }
    }
}
