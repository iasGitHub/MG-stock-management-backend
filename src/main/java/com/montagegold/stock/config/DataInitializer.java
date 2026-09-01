package com.montagegold.stock.config;

import com.montagegold.stock.entity.Supplier;
import com.montagegold.stock.entity.Product;
import com.montagegold.stock.entity.User;
import com.montagegold.stock.enums.Role;
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
    private final PasswordEncoder passwordEncoder;

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
            productRepository.save(Product.builder()
                    .reference("REF-001").name("HP Portable Laptop")
                    .description("HP ProBook 15 inches - i5 8GB RAM")
                    .category("IT").stockQuantity(15).minThreshold(5)
                    .unitPrice(450000.0).build());

            productRepository.save(Product.builder()
                    .reference("REF-002").name("Canon Printer")
                    .description("Mono laser printer")
                    .category("IT").stockQuantity(3).minThreshold(5)
                    .unitPrice(180000.0).build());

            productRepository.save(Product.builder()
                    .reference("REF-003").name("A4 Paper Ream")
                    .description("Office paper 80g - pack of 500 sheets")
                    .category("Stationery").stockQuantity(120).minThreshold(30)
                    .unitPrice(3500.0).build());

            productRepository.save(Product.builder()
                    .reference("REF-004").name("Black Toner")
                    .description("Compatible toner cartridge for Canon")
                    .category("Consumables").stockQuantity(8).minThreshold(10)
                    .unitPrice(25000.0).build());

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
