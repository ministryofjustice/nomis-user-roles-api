package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.From
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail

@Repository
class UserPersonDetailPageRepositoryImpl(@PersistenceContext private val em: EntityManager) : UserPersonDetailPageRepository {

    override fun findPageOfIds(spec: Specification<UserPersonDetail>?, pageable: Pageable): Page<String> {
        val cb = em.criteriaBuilder

        // ----- ID query -----
        val idCq: CriteriaQuery<String> = cb.createQuery(String::class.java)
        val idRoot: Root<UserPersonDetail> = idCq.from(UserPersonDetail::class.java)

        // Apply spec predicate (may add joins; that's ok because we use DISTINCT)
        val idPredicate: Predicate? = spec?.toPredicate(idRoot, idCq, cb)

        // Select only the PK and deduplicate
        idCq.select(idRoot.get("username"))
        idCq.distinct(true)
        if (idPredicate != null) idCq.where(idPredicate)

        // Apply sorting from pageable (supports nested paths like "staff.lastName")
        applySort(pageable.sort, idRoot, idCq, cb)

        // Page slice of IDs
        val idQuery = em.createQuery(idCq)
        idQuery.firstResult = pageable.offset.toInt()
        idQuery.maxResults = pageable.pageSize
        val ids: List<String> = idQuery.resultList

        // Short-circuit empty page
        if (ids.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0)
        }

        // ----- Count query (count distinct usernames) -----
        val countCq: CriteriaQuery<Long> = cb.createQuery(Long::class.java)
        val countRoot: Root<UserPersonDetail> = countCq.from(UserPersonDetail::class.java)
        val countPredicate: Predicate? = spec?.toPredicate(countRoot, countCq, cb)

        val countExpr: Expression<Long> = cb.countDistinct(countRoot.get<String>("username"))
        countCq.select(countExpr)
        if (countPredicate != null) countCq.where(countPredicate)

        val total: Long = em.createQuery(countCq).singleResult

        return PageImpl(ids, pageable, total)
    }

    /**
     * Applies Sort to the CriteriaQuery. Supports dot-notated properties
     * (e.g., "staff.lastName"). Joins are LEFT joins to avoid filtering the set.
     */
    private fun applySort(
        sort: Sort,
        root: Root<UserPersonDetail>,
        query: CriteriaQuery<*>,
        cb: CriteriaBuilder
    ) {
        if (sort.isUnsorted) return

        val orders = sort.map { order ->
            val path = resolvePath(root, order.property)
            if (order.isAscending) cb.asc(path) else cb.desc(path)
        }.toList()

        if (orders.isNotEmpty()) {
            query.orderBy(orders)
        }
    }

    /**
     * Resolves a dot-notated property path to a Criteria Path, joining as needed.
     * Example: "staff.lastName" joins "staff" (LEFT) and gets "lastName".
     */
    @Suppress("UNCHECKED_CAST")
    private fun resolvePath(from: From<*, *>, dotPath: String): Path<*> {
        val parts = dotPath.split(".")
        var currentFrom: From<*, *> = from
        for (i in 0 until parts.size - 1) {
            // Create or reuse join
            val attr = parts[i]
            // Try existing join first (prevents duplicate joins)
            val existing = currentFrom.joins.firstOrNull { it.attribute.name == attr }
            currentFrom = (existing ?: currentFrom.join<Any, Any>(attr, JoinType.LEFT)) as From<*, *>
        }
        return currentFrom.get<Any>(parts.last())
    }
}