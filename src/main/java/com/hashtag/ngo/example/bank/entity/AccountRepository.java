package com.hashtag.ngo.example.bank.entity;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository Spring Data JPA pour {@link Account}.
 * <p>
 * Aucune méthode personnalisée n'est nécessaire pour l'instant : les
 * opérations CRUD fournies par {@link JpaRepository} (save, findById,
 * findAll, ...) suffisent aux besoins du service métier.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {
}
