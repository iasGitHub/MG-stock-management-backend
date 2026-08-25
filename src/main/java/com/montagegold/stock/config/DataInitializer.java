package com.montagegold.stock.config;

import com.montagegold.stock.entity.Fournisseur;
import com.montagegold.stock.entity.Produit;
import com.montagegold.stock.entity.Utilisateur;
import com.montagegold.stock.enums.Role;
import com.montagegold.stock.repository.FournisseurRepository;
import com.montagegold.stock.repository.ProduitRepository;
import com.montagegold.stock.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final ProduitRepository produitRepository;
    private final FournisseurRepository fournisseurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (utilisateurRepository.count() == 0) {
            utilisateurRepository.save(Utilisateur.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .nomComplet("Administrateur")
                    .role(Role.ADMIN)
                    .build());

            utilisateurRepository.save(Utilisateur.builder()
                    .username("gestionnaire")
                    .password(passwordEncoder.encode("gestion123"))
                    .nomComplet("Gestionnaire de Stock")
                    .role(Role.GESTIONNAIRE)
                    .build());

            log.info("Comptes par défaut créés : admin/admin123, gestionnaire/gestion123");
        }

        if (produitRepository.count() == 0) {
            produitRepository.save(Produit.builder()
                    .reference("REF-001").nom("Ordinateur Portable HP")
                    .description("HP ProBook 15 pouces - i5 8Go RAM")
                    .categorie("Informatique").quantiteStock(15).seuilMin(5)
                    .prixUnitaire(450000.0).build());

            produitRepository.save(Produit.builder()
                    .reference("REF-002").nom("Imprimante Canon")
                    .description("Imprimante laser monochrome")
                    .categorie("Informatique").quantiteStock(3).seuilMin(5)
                    .prixUnitaire(180000.0).build());

            produitRepository.save(Produit.builder()
                    .reference("REF-003").nom("Ramette Papier A4")
                    .description("Papier bureautique 80g - paquet de 500 feuilles")
                    .categorie("Fournitures").quantiteStock(120).seuilMin(30)
                    .prixUnitaire(3500.0).build());

            produitRepository.save(Produit.builder()
                    .reference("REF-004").nom("Toner Noir")
                    .description("Cartouche toner compatible Canon")
                    .categorie("Consommables").quantiteStock(8).seuilMin(10)
                    .prixUnitaire(25000.0).build());

            log.info("Produits d'exemple créés");
        }

        if (fournisseurRepository.count() == 0) {
            fournisseurRepository.save(Fournisseur.builder()
                    .code("FRS-001").nom("TechnoImport SARL")
                    .telephone("+222 45 25 12 34")
                    .adresse("Zone industrielle, Nouakchott").build());

            fournisseurRepository.save(Fournisseur.builder()
                    .code("FRS-002").nom("Bureau Plus")
                    .telephone("+222 45 29 87 65")
                    .adresse("Avenue Charles de Gaulle, Nouakchott").build());

            fournisseurRepository.save(Fournisseur.builder()
                    .code("FRS-003").nom("Mauritanie Fournitures")
                    .telephone("+222 46 33 21 09").build());

            log.info("Fournisseurs d'exemple créés");
        }
    }
}
