package ru.vladislavsumin.feature.logViewer.domain.logs

import ru.vladislavsumin.feature.logParser.domain.LogLevel

/**
 * Набор правил для фильтрации.
 *
 * Правила с одинаковым [Field] применяются через операцию ИЛИ.
 * Правила с разными [Field] применяются через операцию И.
 */
data class FilterRequest(val operation: FilterOperation) {

    sealed interface FilterOperation {
        /**
         * Подготавливает фильтр запрос к параллельному исполнению. Тут можно заранее подготовить данные для
         * последующей работы.
         */
        fun prepare(runIdOrders: List<RunIdInfo>?): PreparedFilterOperation?

        /**
         * Простые операции фильтрации, производят обработку данных лог записи. Например, осуществляют фильтрацию по
         * какому то одному из полей [LogRecord].
         */
        sealed interface Simple : FilterOperation

        data object NoOp : Simple, PreparedFilterOperation {
            override fun prepare(runIdOrders: List<RunIdInfo>?): PreparedFilterOperation = this
            override fun check(record: LogRecord): Boolean = true
        }

        data class MinLogLevel(private val minLevel: LogLevel) : Simple, PreparedFilterOperation {
            override fun prepare(runIdOrders: List<RunIdInfo>?): PreparedFilterOperation = this
            override fun check(record: LogRecord): Boolean = record.logLevel.rawLevel >= minLevel.rawLevel
        }

        data class RunNumber(private val number: Int) : Simple {
            override fun prepare(runIdOrders: List<RunIdInfo>?): PreparedFilterOperation? {
                val orders = runIdOrders?.getOrNull(number)?.orderRange
                return orders?.let { PreparedRunOrder(orders) }
            }

            private class PreparedRunOrder(private val orders: IntRange) : PreparedFilterOperation {
                override fun check(record: LogRecord): Boolean = record.order in orders
            }
        }

        data class TimeAfter(private val time: String) : Simple, PreparedFilterOperation {
            override fun prepare(runIdOrders: List<RunIdInfo>?): PreparedFilterOperation = this
            override fun check(record: LogRecord): Boolean = record.raw.substring(record.time) >= time
        }

        data class TimeBefore(private val time: String) : Simple, PreparedFilterOperation {
            override fun prepare(runIdOrders: List<RunIdInfo>?): PreparedFilterOperation = this
            override fun check(record: LogRecord): Boolean = record.raw.substring(record.time) <= time
        }

        data class All(private val operation: Operation) : Simple, PreparedFilterOperation {
            override fun prepare(runIdOrders: List<RunIdInfo>?): PreparedFilterOperation = this
            override fun check(record: LogRecord): Boolean = operation.check(record.raw)
        }

        data class Tag(private val operation: Operation) : Simple, PreparedFilterOperation {
            override fun prepare(runIdOrders: List<RunIdInfo>?): PreparedFilterOperation = this
            override fun check(record: LogRecord): Boolean = operation.check(record.raw.substring(record.tag))
        }

        data class ProcessId(private val operation: Operation) : Simple, PreparedFilterOperation {
            override fun prepare(runIdOrders: List<RunIdInfo>?): PreparedFilterOperation = this
            override fun check(record: LogRecord): Boolean =
                record.processId?.let { operation.check(record.raw.substring(it)) } ?: false
        }

        data class Thread(private val operation: Operation) : Simple, PreparedFilterOperation {
            override fun prepare(runIdOrders: List<RunIdInfo>?): PreparedFilterOperation = this
            override fun check(record: LogRecord): Boolean = operation.check(record.raw.substring(record.thread))
        }

        data class Message(private val operation: Operation) : Simple, PreparedFilterOperation {
            override fun prepare(runIdOrders: List<RunIdInfo>?): PreparedFilterOperation = this
            override fun check(record: LogRecord): Boolean = operation.check(record.raw.substring(record.message))
        }

        /**
         * Составные фильтр операции. Объединяют одну или несколько операций в цепочку.
         */
        sealed interface Composite : FilterOperation

        @ConsistentCopyVisibility
        data class Auto private constructor(private val operation: FilterOperation) : Composite {
            constructor(operations: List<FilterOperation>) : this(mapOperation(operations))

            override fun prepare(runIdOrders: List<RunIdInfo>?): PreparedFilterOperation? =
                operation.prepare(runIdOrders)

            companion object {
                private fun mapOperation(operations: List<FilterOperation>): FilterOperation {
                    val ors = operations
                        .groupBy { it::class }
                        .map { (_, ops) -> ops.singleOrNull() ?: Or(ops) }
                    return when (ors.size) {
                        0 -> NoOp
                        1 -> ors[0]
                        else -> And(ors)
                    }
                }
            }
        }

        data class And(private val operations: List<FilterOperation>) : Composite {
            override fun prepare(runIdOrders: List<RunIdInfo>?): PreparedFilterOperation = PreparedAnd(
                operations.mapNotNull { it.prepare(runIdOrders) },
            )

            private class PreparedAnd(
                private val preparedOperations: List<PreparedFilterOperation>,
            ) : PreparedFilterOperation {
                override fun check(record: LogRecord): Boolean = preparedOperations.all { it.check(record) }
            }
        }

        data class Or(private val operations: List<FilterOperation>) : Composite {
            override fun prepare(runIdOrders: List<RunIdInfo>?): PreparedFilterOperation = PreparedOr(
                operations.mapNotNull { it.prepare(runIdOrders) },
            )

            private class PreparedOr(
                private val preparedOperations: List<PreparedFilterOperation>,
            ) : PreparedFilterOperation {
                override fun check(record: LogRecord): Boolean = preparedOperations.any { it.check(record) }
            }
        }

        data class Not(private val operation: FilterOperation) : Composite {
            override fun prepare(runIdOrders: List<RunIdInfo>?): PreparedFilterOperation? =
                operation.prepare(runIdOrders)?.let { PreparedNot(it) }

            private class PreparedNot(
                private val preparedOperation: PreparedFilterOperation,
            ) : PreparedFilterOperation {
                override fun check(record: LogRecord): Boolean = !preparedOperation.check(record)
            }
        }
    }

    interface PreparedFilterOperation {
        fun check(record: LogRecord): Boolean
    }

    /**
     * Операция поиска.
     */
    sealed interface Operation {
        fun check(record: String): Boolean

        data class Exactly(private val data: String) : Operation {
            override fun check(record: String): Boolean = record == data
        }

        data class Contains(private val data: String) : Operation {
            override fun check(record: String): Boolean = record.contains(data, ignoreCase = true)
        }
    }
}
