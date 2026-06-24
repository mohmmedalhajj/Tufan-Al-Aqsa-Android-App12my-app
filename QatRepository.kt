package com.example.data

import com.example.util.Helpers
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class QatRepository(private val db: AppDatabase) {

    private val taxDao = db.taxDao()
    private val inventoryDao = db.inventoryDao()
    private val salesDao = db.salesDao()
    private val customerDao = db.customerDao()
    private val supplierDao = db.supplierDao()
    private val expenseDao = db.expenseDao()
    private val transferDao = db.transferDao()

    // --- Flows ---
    val allTax: Flow<List<TaxEntry>> = taxDao.getAllTax()
    val allInventoryItems: Flow<List<InventoryItem>> = inventoryDao.getAllItems()
    val allInvoices: Flow<List<SalesInvoice>> = salesDao.getAllInvoices()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allSuppliers: Flow<List<Supplier>> = supplierDao.getAllSuppliers()
    val allPurchases: Flow<List<SupplierPurchase>> = supplierDao.getAllPurchases()
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allPaymentsCode: Flow<List<CustomerPayment>> = customerDao.getAllPayments()
    val allTransfers: Flow<List<MoneyTransfer>> = transferDao.getAllTransfers()
    val allPurchaseItems: Flow<List<SupplierPurchaseItem>> = supplierDao.getAllPurchaseItems()
    val allInvoiceItems: Flow<List<SalesInvoiceItem>> = salesDao.getAllInvoiceItems()

    // --- Tax Operations ---
    suspend fun getTaxAmountForDate(date: String): Double {
        return taxDao.getTaxByDate(date)?.taxAmount ?: 0.0
    }

    suspend fun setTaxForDate(date: String, amount: Double) {
        val existing = taxDao.getTaxByDate(date)
        if (existing != null) {
            taxDao.insertTax(existing.copy(taxAmount = amount))
        } else {
            taxDao.insertTax(TaxEntry(date = date, taxAmount = amount))
        }
    }

    suspend fun getTaxEntryForDate(date: String): TaxEntry? {
        return taxDao.getTaxByDate(date)
    }

    suspend fun saveTaxEntry(taxEntry: TaxEntry) {
        taxDao.insertTax(taxEntry)
    }

    // --- Inventory Operations ---
    suspend fun addOrUpdateInventoryItem(item: InventoryItem): Long {
        return inventoryDao.insertItem(item)
    }

    suspend fun deleteInventoryItem(item: InventoryItem) {
        inventoryDao.deleteItemById(item.id)
    }

    suspend fun getItemById(id: Int): InventoryItem? {
        return inventoryDao.getItemById(id)
    }

    // --- Sales Operations with stock reduction & debt accumulation ---
    suspend fun makeSale(
        customerName: String,
        date: String, // yyyy-MM-dd HH:mm
        items: List<Pair<InventoryItem, Double>>, // Item & Quantity sold
        taxPercent: Double, // Daily flat tax applied or custom
        discount: Double,
        paidAmount: Double,
        paymentMethod: String = "نقداً",
        currency: String = "ريال يمني",
        paidAmountYer: Double = 0.0,
        paidAmountSar: Double = 0.0,
        paymentMethodYer: String = "نقداً",
        paymentMethodSar: String = "نقداً",
        exchangeRate: Double = 1.0
    ): Long {
        return db.withTransaction {
            val subtotalYer = items.filter {
                val activeCurrency = if (it.first.notes.startsWith("sale_currency:")) {
                    it.first.notes.substringAfter("sale_currency:").substringBefore("|")
                } else {
                    it.first.buyPriceCurrency.ifEmpty { "ريال يمني" }
                }
                activeCurrency.contains("يمني")
            }.sumOf { it.first.sellPrice * it.second }

            val subtotalSar = items.filter {
                val activeCurrency = if (it.first.notes.startsWith("sale_currency:")) {
                    it.first.notes.substringAfter("sale_currency:").substringBefore("|")
                } else {
                    it.first.buyPriceCurrency.ifEmpty { "ريال يمني" }
                }
                activeCurrency.contains("سعودي")
            }.sumOf { it.first.sellPrice * it.second }

            var finalPaidYer = paidAmountYer
            var finalPaidSar = paidAmountSar

            // Compute YER paid based on its specific payment method
            if (subtotalYer > 0.0) {
                if (paymentMethodYer in listOf("نقداً", "تحويل", "إيداع")) {
                    finalPaidYer = subtotalYer
                } else if (paymentMethodYer == "آجل") {
                    finalPaidYer = 0.0
                }
            } else {
                finalPaidYer = 0.0
            }

            // Compute SAR paid based on its specific payment method
            if (subtotalSar > 0.0) {
                if (paymentMethodSar in listOf("نقداً", "تحويل", "إيداع")) {
                    finalPaidSar = subtotalSar
                } else if (paymentMethodSar == "آجل") {
                    finalPaidSar = 0.0
                }
            } else {
                finalPaidSar = 0.0
            }

            // Overrides if paid value is precisely supplied (such as in partial payment)
            if (paymentMethodYer == "دفع جزئي" && paidAmountYer > 0.0) {
                finalPaidYer = paidAmountYer
            }
            if (paymentMethodSar == "دفع جزئي" && paidAmountSar > 0.0) {
                finalPaidSar = paidAmountSar
            }

            // Fallbacks for legacy single-currency calls
            if (subtotalYer > 0.0 && subtotalSar == 0.0 && paidAmount > 0.0 && finalPaidYer == 0.0) {
                finalPaidYer = paidAmount
            }
            if (subtotalSar > 0.0 && subtotalYer == 0.0 && paidAmount > 0.0 && finalPaidSar == 0.0) {
                finalPaidSar = paidAmount
            }

            val debtYer = if (subtotalYer > finalPaidYer) subtotalYer - finalPaidYer else 0.0
            val debtSar = if (subtotalSar > finalPaidSar) subtotalSar - finalPaidSar else 0.0

            val computedTotal = subtotalYer + subtotalSar
            val computedPaid = finalPaidYer + finalPaidSar
            val computedDebt = debtYer + debtSar

            val calculatedCurrency = if (subtotalYer > 0.0 && subtotalSar > 0.0) "ريال يمني / ريال سعودي" else if (subtotalSar > 0.0) "ريال سعودي" else "ريال يمني"
            val calculatedPaymentMethod = if (subtotalYer > 0.0 && subtotalSar > 0.0) {
                "يمني: $paymentMethodYer | سعودي: $paymentMethodSar"
            } else if (subtotalSar > 0.0) {
                paymentMethodSar
            } else {
                paymentMethodYer
            }

            // Create Invoice
            val invoice = SalesInvoice(
                customerName = customerName,
                date = date,
                totalAmount = computedTotal,
                taxApplied = 0.0,
                discount = 0.0,
                paidAmount = computedPaid,
                debtAmount = computedDebt,
                paymentMethod = calculatedPaymentMethod,
                currency = calculatedCurrency,
                totalAmountYer = subtotalYer,
                totalAmountSar = subtotalSar,
                paidAmountYer = finalPaidYer,
                paidAmountSar = finalPaidSar,
                debtAmountYer = debtYer,
                debtAmountSar = debtSar,
                paymentMethodYer = paymentMethodYer,
                paymentMethodSar = paymentMethodSar
            )
            val invoiceId = salesDao.insertInvoice(invoice).toInt()

            // Save items & update stock
            for (p in items) {
                val stockItem = p.first
                val qty = p.second
                val origCurrency = stockItem.buyPriceCurrency.ifEmpty { "ريال يمني" }
                val activeSaleCurrency = if (stockItem.notes.startsWith("sale_currency:")) {
                    stockItem.notes.substringAfter("sale_currency:").substringBefore("|")
                } else {
                    origCurrency
                }
                
                val isBuySar = origCurrency.contains("سعودي")
                val isSaleSar = activeSaleCurrency.contains("سعودي")
                val computedRealProfit = if (isBuySar && !isSaleSar) {
                    val buyPriceConverted = stockItem.buyPrice * exchangeRate
                    (stockItem.sellPrice - buyPriceConverted) * qty
                } else if (!isBuySar && isSaleSar) {
                    val buyPriceConverted = if (exchangeRate > 0.0) stockItem.buyPrice / exchangeRate else stockItem.buyPrice
                    (stockItem.sellPrice - buyPriceConverted) * qty
                } else {
                    (stockItem.sellPrice - stockItem.buyPrice) * qty
                }

                val saleItem = SalesInvoiceItem(
                    invoiceId = invoiceId,
                    itemId = stockItem.id,
                    itemName = stockItem.name,
                    quantity = qty,
                    unitPrice = stockItem.sellPrice,
                    buyPrice = stockItem.buyPrice,
                    currency = activeSaleCurrency,
                    originalCurrency = origCurrency,
                    saleCurrency = activeSaleCurrency,
                    exchangeRateUsed = exchangeRate,
                    originalBuyPrice = stockItem.buyPrice,
                    actualSalePrice = stockItem.sellPrice,
                    realProfit = computedRealProfit
                )
                salesDao.insertInvoiceItem(saleItem)
                
                // Deduct stock
                inventoryDao.updateQuantity(stockItem.id, -qty)
            }

            // Update customer debt if customer name matches a registered customer
            if (customerName.trim().isNotEmpty() && customerName.trim() != "عميل نقدي") {
                val clientList = customerDao.getAllCustomersDirect()
                val registeredCust = clientList.find { it.name.trim().equals(customerName.trim(), ignoreCase = true) }
                if (registeredCust != null) {
                    if (computedDebt > 0.0) {
                        customerDao.updateCustomerDebt(registeredCust.id, computedDebt)
                    }
                } else {
                    // Create customer automatically!
                    customerDao.insertCustomer(
                        Customer(name = customerName.trim(), phone = "", totalDebt = computedDebt)
                    )
                }
            }

            invoiceId.toLong()
        }
    }

    suspend fun deleteSale(invoiceId: Int) {
        db.withTransaction {
            val invoice = salesDao.getInvoiceById(invoiceId) ?: return@withTransaction
            val items = salesDao.getInvoiceItemsDirect(invoiceId)
            
            // Revert stock changes
            for (item in items) {
                inventoryDao.updateQuantity(item.itemId, item.quantity)
            }

            // Revert customer debt
            if (invoice.debtAmount > 0.0) {
                val clientList = customerDao.getAllCustomersDirect()
                val registeredCust = clientList.find { it.name.trim().equals(invoice.customerName.trim(), ignoreCase = true) }
                if (registeredCust != null) {
                    customerDao.updateCustomerDebt(registeredCust.id, -invoice.debtAmount)
                }
            }

            // Wipe records
            salesDao.deleteInvoiceById(invoiceId)
            salesDao.deleteInvoiceItemsByInvoiceId(invoiceId)
        }
    }

    fun getSaleItems(invoiceId: Int): Flow<List<SalesInvoiceItem>> {
        return salesDao.getInvoiceItems(invoiceId)
    }

    suspend fun updateInvoice(
        invoiceId: Int,
        newCustomerName: String,
        newPaidAmount: Double,
        newPaymentMethod: String
    ) {
        db.withTransaction {
            val invoice = salesDao.getInvoiceById(invoiceId) ?: return@withTransaction
            val oldDebt = invoice.debtAmount
            
            // Recalculate debt
            val total = invoice.totalAmount
            val newDebt = if (total > newPaidAmount) total - newPaidAmount else 0.0
            
            // Update customer balance if debt changed
            val clientList = customerDao.getAllCustomersDirect()
            
            // 1. Revert old debt from old customer
            val oldCust = clientList.find { it.name.trim().equals(invoice.customerName.trim(), ignoreCase = true) }
            if (oldCust != null && oldDebt > 0.0) {
                customerDao.updateCustomerDebt(oldCust.id, -oldDebt)
            }
            
            // 2. Apply new debt to new customer
            val newCust = clientList.find { it.name.trim().equals(newCustomerName.trim(), ignoreCase = true) }
            if (newCust != null && newDebt > 0.0) {
                customerDao.updateCustomerDebt(newCust.id, newDebt)
            } else if (newDebt > 0.0 && newCustomerName.trim().isNotEmpty() && newCustomerName.trim() != "عميل نقدي") {
                // Create new customer
                customerDao.insertCustomer(
                    Customer(name = newCustomerName.trim(), phone = "", totalDebt = newDebt)
                )
            }
            
            // 3. Update the invoice record
            val updatedInvoice = invoice.copy(
                customerName = newCustomerName,
                paidAmount = newPaidAmount,
                debtAmount = newDebt,
                paymentMethod = newPaymentMethod
            )
            salesDao.insertInvoice(updatedInvoice)
        }
    }

    // --- Customer operations ---
    suspend fun getAllCustomersDirect(): List<Customer> {
        return customerDao.getAllCustomersDirect()
    }

    suspend fun addCustomer(customer: Customer): Long {
        return customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomerById(customer.id)
    }

    suspend fun recordDirectCustomerDebt(
        customerName: String,
        amount: Double,
        date: String,
        statement: String,
        notes: String,
        currency: String = "ريال يمني"
    ): Long {
        return db.withTransaction {
            val paymentMethod = if (notes.trim().isNotEmpty()) {
                "دين مباشر: $statement ($notes)"
            } else {
                "دين مباشر: $statement"
            }
            val isYer = currency.contains("يمني")
            val isSar = currency.contains("سعودي")
            val amountYer = if (isYer) amount else 0.0
            val amountSar = if (isSar) amount else 0.0
            val invoice = SalesInvoice(
                customerName = customerName,
                date = date,
                totalAmount = amount,
                taxApplied = 0.0,
                discount = 0.0,
                paidAmount = 0.0,
                debtAmount = amount,
                paymentMethod = paymentMethod,
                currency = currency,
                totalAmountYer = amountYer,
                totalAmountSar = amountSar,
                paidAmountYer = 0.0,
                paidAmountSar = 0.0,
                debtAmountYer = amountYer,
                debtAmountSar = amountSar
            )
            val invoiceId = salesDao.insertInvoice(invoice)
            
            // Update customer debt
            if (customerName.trim().isNotEmpty() && customerName.trim() != "عميل نقدي") {
                val clientList = customerDao.getAllCustomersDirect()
                val registeredCust = clientList.find { it.name.trim().equals(customerName.trim(), ignoreCase = true) }
                if (registeredCust != null) {
                    customerDao.updateCustomerDebt(registeredCust.id, amount)
                } else {
                    customerDao.insertCustomer(
                        Customer(name = customerName.trim(), phone = "", totalDebt = amount)
                    )
                }
            }
            invoiceId
        }
    }

    suspend fun payCustomerDebt(customerId: Int, amount: Double, date: String, notes: String, paymentMethod: String = "نقداً", currency: String = "ريال يمني") {
        db.withTransaction {
            val pay = CustomerPayment(
                customerId = customerId,
                amount = amount,
                date = date,
                notes = notes,
                paymentMethod = paymentMethod,
                currency = currency
            )
            customerDao.insertPayment(pay)
            customerDao.updateCustomerDebt(customerId, -amount)
        }
    }

    fun getPaymentsForCustomer(customerId: Int): Flow<List<CustomerPayment>> {
        return customerDao.getPaymentsForCustomer(customerId)
    }

    // --- Supplier & purchase operations ---
    suspend fun addSupplier(supplier: Supplier): Long {
        return supplierDao.insertSupplier(supplier)
    }

    suspend fun updateSupplier(supplier: Supplier) {
        supplierDao.updateSupplier(supplier)
    }

    suspend fun deleteSupplier(supplier: Supplier) {
        supplierDao.deleteSupplierById(supplier.id)
    }

    suspend fun makeSupplierPurchase(
        supplierId: Int,
        date: String,
        itemsPriceAndQty: List<Triple<InventoryItem, Double, Double>>, // Item, Quantity, PurchaseUnitPrice
        paidAmount: Double,
        paymentMethod: String = "نقداً",
        paidAmountYer: Double = 0.0,
        paidAmountSar: Double = 0.0,
        paymentMethodYer: String = "نقداً",
        paymentMethodSar: String = "نقداً"
    ): Long {
        return db.withTransaction {
            var totalBillYer = 0.0
            var totalBillSar = 0.0
            for (trip in itemsPriceAndQty) {
                val stockItem = trip.first
                val qty = trip.second
                val buyPrice = trip.third
                val itemCurrency = (stockItem.buyPriceCurrency.ifEmpty { "ريال يمني" })
                if (itemCurrency.contains("سعودي")) {
                    totalBillSar += qty * buyPrice
                } else {
                    totalBillYer += qty * buyPrice
                }
            }

            val totalBill = totalBillYer + totalBillSar

            var finalPaidYer = paidAmountYer
            var finalPaidSar = paidAmountSar

            // Compute YER paid based on its specific payment method
            if (totalBillYer > 0.0) {
                if (paymentMethodYer in listOf("نقداً", "تحويل", "إيداع")) {
                    finalPaidYer = totalBillYer
                } else if (paymentMethodYer == "آجل") {
                    finalPaidYer = 0.0
                }
            } else {
                finalPaidYer = 0.0
            }

            // Compute SAR paid based on its specific payment method
            if (totalBillSar > 0.0) {
                if (paymentMethodSar in listOf("نقداً", "تحويل", "إيداع")) {
                    finalPaidSar = totalBillSar
                } else if (paymentMethodSar == "آجل") {
                    finalPaidSar = 0.0
                }
            } else {
                finalPaidSar = 0.0
            }

            // Overrides if paid value is precisely supplied (such as in partial payment)
            if (paymentMethodYer == "دفع جزئي" && paidAmountYer > 0.0) {
                finalPaidYer = paidAmountYer
            }
            if (paymentMethodSar == "دفع جزئي" && paidAmountSar > 0.0) {
                finalPaidSar = paidAmountSar
            }

            // Fallbacks for legacy single-currency calls
            if (totalBillYer > 0.0 && totalBillSar == 0.0 && paidAmount > 0.0 && finalPaidYer == 0.0) {
                finalPaidYer = paidAmount
            }
            if (totalBillSar > 0.0 && totalBillYer == 0.0 && paidAmount > 0.0 && finalPaidSar == 0.0) {
                finalPaidSar = paidAmount
            }

            val debtYer = if (totalBillYer > finalPaidYer) totalBillYer - finalPaidYer else 0.0
            val debtSar = if (totalBillSar > finalPaidSar) totalBillSar - finalPaidSar else 0.0
            val debtTotal = debtYer + debtSar

            val isDual = totalBillYer > 0.0 && totalBillSar > 0.0
            val inferredCurrency = if (isDual) "ريال يمني / ريال سعودي" else if (totalBillSar > 0.0) "ريال سعودي" else "ريال يمني"
            val calculatedPaymentMethod = if (totalBillYer > 0.0 && totalBillSar > 0.0) {
                "يمني: $paymentMethodYer | سعودي: $paymentMethodSar"
            } else if (totalBillSar > 0.0) {
                paymentMethodSar
            } else {
                paymentMethodYer
            }

            val purchase = SupplierPurchase(
                supplierId = supplierId,
                date = date,
                totalAmount = totalBill,
                paidAmount = finalPaidYer + finalPaidSar,
                debtRemaining = debtTotal,
                paymentMethod = calculatedPaymentMethod,
                currency = inferredCurrency,
                totalAmountYer = totalBillYer,
                totalAmountSar = totalBillSar,
                paidAmountYer = finalPaidYer,
                paidAmountSar = finalPaidSar,
                debtRemainingYer = debtYer,
                debtRemainingSar = debtSar,
                paymentMethodYer = paymentMethodYer,
                paymentMethodSar = paymentMethodSar
            )
            val purchaseId = supplierDao.insertPurchase(purchase).toInt()

            for (trip in itemsPriceAndQty) {
                val stockItem = trip.first
                val qty = trip.second
                val buyPrice = trip.third
                val itemCurrency = if (stockItem.buyPriceCurrency.ifEmpty { "ريال يمني" }.contains("سعودي")) "ريال سعودي" else "ريال يمني"

                // Add purchase item record
                val pItem = SupplierPurchaseItem(
                    purchaseId = purchaseId,
                    itemId = stockItem.id,
                    itemName = stockItem.name,
                    quantity = qty,
                    unitPrice = buyPrice,
                    currency = itemCurrency
                )
                supplierDao.insertPurchaseItem(pItem)

                // Update stock and update average buyPrice!
                val existing = inventoryDao.getItemById(stockItem.id)
                if (existing != null) {
                    val newQuantity = existing.quantity + qty
                    val newBuyPrice = if (newQuantity > 0) {
                        ((existing.quantity * existing.buyPrice) + (qty * buyPrice)) / newQuantity
                    } else buyPrice
                    
                    inventoryDao.updateItem(
                        existing.copy(
                            quantity = newQuantity,
                            buyPrice = newBuyPrice,
                            buyPriceCurrency = itemCurrency,
                            dateAdded = existing.dateAdded.ifEmpty { date.ifEmpty { Helpers.getCurrentDateTime() } }
                        )
                    )
                } else {
                    // Create new stock item
                    inventoryDao.insertItem(
                        InventoryItem(
                            id = stockItem.id,
                            name = stockItem.name,
                            quantity = qty,
                            buyPrice = buyPrice,
                            sellPrice = stockItem.sellPrice,
                            lowStockThreshold = stockItem.lowStockThreshold,
                            buyPriceCurrency = itemCurrency,
                            dateAdded = date.ifEmpty { Helpers.getCurrentDateTime() }
                        )
                    )
                }
            }

            if (debtTotal > 0.0) {
                supplierDao.updateSupplierBalance(supplierId, debtTotal)
            }

            purchaseId.toLong()
        }
    }

    suspend fun makeSupplierReturn(
        supplierId: Int,
        itemId: Int,
        itemName: String,
        returnedQty: Double,
        returnedPrice: Double,
        returnDate: String,
        refundedAmount: Double,
        notes: String
    ): Long {
        return db.withTransaction {
            val totalValue = returnedQty * returnedPrice
            val debtChange = -(totalValue - refundedAmount)

            val stockItem = inventoryDao.getItemById(itemId)
            val inferredCurrency = stockItem?.buyPriceCurrency ?: "ريال يمني"
            val isSar = inferredCurrency.contains("سعودي")

            val returnPurchase = SupplierPurchase(
                supplierId = supplierId,
                date = returnDate,
                totalAmount = -totalValue,
                paidAmount = -refundedAmount,
                debtRemaining = debtChange,
                paymentMethod = if (notes.trim().isNotEmpty()) "مرتجع: $notes" else "مرتجع بضاعة",
                currency = inferredCurrency,
                totalAmountYer = if (isSar) 0.0 else -totalValue,
                totalAmountSar = if (isSar) -totalValue else 0.0,
                paidAmountYer = if (isSar) 0.0 else -refundedAmount,
                paidAmountSar = if (isSar) -refundedAmount else 0.0,
                debtRemainingYer = if (isSar) 0.0 else debtChange,
                debtRemainingSar = if (isSar) debtChange else 0.0,
                paymentMethodYer = if (isSar) "لا يوجد" else (if (notes.trim().isNotEmpty()) "مرتجع: $notes" else "مرتجع"),
                paymentMethodSar = if (isSar) (if (notes.trim().isNotEmpty()) "مرتجع: $notes" else "مرتجع") else "لا يوجد"
            )
            val purchaseId = supplierDao.insertPurchase(returnPurchase).toInt()

            val pItem = SupplierPurchaseItem(
                purchaseId = purchaseId,
                itemId = itemId,
                itemName = itemName,
                quantity = -returnedQty,
                unitPrice = returnedPrice,
                currency = inferredCurrency
            )
            supplierDao.insertPurchaseItem(pItem)

            inventoryDao.updateQuantity(itemId, -returnedQty)
            supplierDao.updateSupplierBalance(supplierId, debtChange)

            purchaseId.toLong()
        }
    }

    suspend fun deletePurchase(purchaseId: Int) {
        db.withTransaction {
            val p = supplierDao.getAllPurchases().firstOrNull()?.find { it.id == purchaseId } ?: return@withTransaction
            val items = supplierDao.getPurchaseItemsDirect(purchaseId)

            for (item in items) {
                // Return stock
                inventoryDao.updateQuantity(item.itemId, -item.quantity)
            }

            if (p.debtRemaining > 0.0) {
                supplierDao.updateSupplierBalance(p.supplierId, -p.debtRemaining)
            }

            supplierDao.deletePurchaseById(purchaseId)
            supplierDao.deletePurchaseItemsByPurchaseId(purchaseId)
        }
    }

    suspend fun paySupplierInvoice(
        purchaseId: Int,
        payAmount: Double,
        currency: String, // "ريال يمني" or "ريال سعودي"
        paymentDetails: String,
        dateOfPayment: String
    ) {
        db.withTransaction {
            val p = supplierDao.getAllPurchases().firstOrNull()?.find { it.id == purchaseId } ?: return@withTransaction
            
            // Revert original debt remaining from supplier balance before changing
            if (p.debtRemaining != 0.0) {
                supplierDao.updateSupplierBalance(p.supplierId, -p.debtRemaining)
            }
            
            val isSar = currency.contains("سعودي")
            val updated = if (isSar) {
                val newPaidSar = p.paidAmountSar + payAmount
                val newDebtSar = p.debtRemainingSar - payAmount
                p.copy(
                    paidAmount = p.paidAmount + payAmount,
                    debtRemaining = p.debtRemaining - payAmount,
                    paidAmountSar = newPaidSar,
                    debtRemainingSar = newDebtSar,
                    paymentMethodSar = p.paymentMethodSar + " | سداد: $paymentDetails ($dateOfPayment)",
                    paymentMethod = p.paymentMethod + " | سداد سعودي بقيمة $payAmount ($paymentDetails - $dateOfPayment)"
                )
            } else {
                val newPaidYer = p.paidAmountYer + payAmount
                val newDebtYer = p.debtRemainingYer - payAmount
                p.copy(
                    paidAmount = p.paidAmount + payAmount,
                    debtRemaining = p.debtRemaining - payAmount,
                    paidAmountYer = newPaidYer,
                    debtRemainingYer = newDebtYer,
                    paymentMethodYer = p.paymentMethodYer + " | سداد: $paymentDetails ($dateOfPayment)",
                    paymentMethod = p.paymentMethod + " | سداد يمني بقيمة $payAmount ($paymentDetails - $dateOfPayment)"
                )
            }
            
            // Save updated purchase
            supplierDao.insertPurchase(updated)
            
            // Re-apply the new remaining debt to supplier balance
            if (updated.debtRemaining != 0.0) {
                supplierDao.updateSupplierBalance(updated.supplierId, updated.debtRemaining)
            }
        }
    }

    suspend fun updatePurchaseInvoice(
        purchaseId: Int,
        newPaidAmount: Double,
        newPaymentMethod: String
    ) {
        db.withTransaction {
            val p = supplierDao.getAllPurchases().firstOrNull()?.find { it.id == purchaseId } ?: return@withTransaction
            
            // 1. Revert previous supplier debt from supplier balance
            if (p.debtRemaining > 0.0) {
                supplierDao.updateSupplierBalance(p.supplierId, -p.debtRemaining)
            }
            
            // 2. Compute new remaining debt
            val newDebt = if (p.totalAmount > newPaidAmount) p.totalAmount - newPaidAmount else 0.0
            
            // 3. Apply new remaining debt to supplier balance
            if (newDebt > 0.0) {
                supplierDao.updateSupplierBalance(p.supplierId, newDebt)
            }
            
            // 4. Save updated purchase record
            val updated = p.copy(
                paidAmount = newPaidAmount,
                debtRemaining = newDebt,
                paymentMethod = newPaymentMethod
            )
            supplierDao.insertPurchase(updated)
        }
    }

    suspend fun editSupplierPurchaseFull(
        purchaseId: Int,
        newSupplierId: Int,
        newDate: String,
        newItemsPriceAndQty: List<Triple<InventoryItem, Double, Double>>,
        newPaidAmount: Double,
        newPaymentMethod: String,
        paidAmountYer: Double = 0.0,
        paidAmountSar: Double = 0.0,
        paymentMethodYer: String = "نقداً",
        paymentMethodSar: String = "نقداً"
    ) {
        db.withTransaction {
            val oldPurchase = supplierDao.getAllPurchases().firstOrNull()?.find { it.id == purchaseId } ?: return@withTransaction
            val oldItems = supplierDao.getPurchaseItemsDirect(purchaseId)
            
            // Revert old stock changes
            for (item in oldItems) {
                inventoryDao.updateQuantity(item.itemId, -item.quantity)
            }
            
            // Revert old debt from old supplier
            if (oldPurchase.debtRemaining > 0.0) {
                supplierDao.updateSupplierBalance(oldPurchase.supplierId, -oldPurchase.debtRemaining)
            }
            
            var totalBillYer = 0.0
            var totalBillSar = 0.0
            for (trip in newItemsPriceAndQty) {
                val stockItem = trip.first
                val qty = trip.second
                val buyPrice = trip.third
                val itemCurrency = (stockItem.buyPriceCurrency.ifEmpty { "ريال يمني" })
                if (itemCurrency.contains("سعودي")) {
                    totalBillSar += qty * buyPrice
                } else {
                    totalBillYer += qty * buyPrice
                }
            }

            val newTotalBill = totalBillYer + totalBillSar
            val isDual = totalBillYer > 0.0 && totalBillSar > 0.0

            var finalPaidYer = paidAmountYer
            var finalPaidSar = paidAmountSar

            // Compute YER paid based on its specific payment method
            if (totalBillYer > 0.0) {
                if (paymentMethodYer in listOf("نقداً", "تحويل", "إيداع")) {
                    finalPaidYer = totalBillYer
                } else if (paymentMethodYer == "آجل") {
                    finalPaidYer = 0.0
                }
            } else {
                finalPaidYer = 0.0
            }

            // Compute SAR paid based on its specific payment method
            if (totalBillSar > 0.0) {
                if (paymentMethodSar in listOf("نقداً", "تحويل", "إيداع")) {
                    finalPaidSar = totalBillSar
                } else if (paymentMethodSar == "آجل") {
                    finalPaidSar = 0.0
                }
            } else {
                finalPaidSar = 0.0
            }

            // Overrides if paid value is precisely supplied (such as in partial payment)
            if (paymentMethodYer == "دفعة جزئية" && paidAmountYer > 0.0) {
                finalPaidYer = paidAmountYer
            }
            if (paymentMethodSar == "دفعة جزئية" && paidAmountSar > 0.0) {
                finalPaidSar = paidAmountSar
            }

            // Fallbacks for legacy single-currency calls
            if (totalBillYer > 0.0 && totalBillSar == 0.0 && newPaidAmount > 0.0 && finalPaidYer == 0.0) {
                finalPaidYer = newPaidAmount
            }
            if (totalBillSar > 0.0 && totalBillYer == 0.0 && newPaidAmount > 0.0 && finalPaidSar == 0.0) {
                finalPaidSar = newPaidAmount
            }

            val debtYer = if (totalBillYer > finalPaidYer) totalBillYer - finalPaidYer else 0.0
            val debtSar = if (totalBillSar > finalPaidSar) totalBillSar - finalPaidSar else 0.0
            val debtTotal = debtYer + debtSar

            val inferredCurrency = if (isDual) "ريال يمني / ريال سعودي" else if (totalBillSar > 0.0) "ريال سعودي" else "ريال يمني"
            val calculatedPaymentMethod = if (totalBillYer > 0.0 && totalBillSar > 0.0) {
                "يمني: $paymentMethodYer | سعودي: $paymentMethodSar"
            } else if (totalBillSar > 0.0) {
                paymentMethodSar
            } else {
                paymentMethodYer
            }

            val updatedPurchase = oldPurchase.copy(
                supplierId = newSupplierId,
                date = newDate,
                totalAmount = newTotalBill,
                paidAmount = finalPaidYer + finalPaidSar,
                debtRemaining = debtTotal,
                paymentMethod = calculatedPaymentMethod,
                currency = inferredCurrency,
                totalAmountYer = totalBillYer,
                totalAmountSar = totalBillSar,
                paidAmountYer = finalPaidYer,
                paidAmountSar = finalPaidSar,
                debtRemainingYer = debtYer,
                debtRemainingSar = debtSar,
                paymentMethodYer = paymentMethodYer,
                paymentMethodSar = paymentMethodSar
            )
            supplierDao.insertPurchase(updatedPurchase)
            
            supplierDao.deletePurchaseItemsByPurchaseId(purchaseId)
            
            for (trip in newItemsPriceAndQty) {
                val stockItem = trip.first
                val qty = trip.second
                val buyPrice = trip.third
                val itemCurrency = if (stockItem.buyPriceCurrency.ifEmpty { "ريال يمني" }.contains("سعودي")) "ريال سعودي" else "ريال يمني"
                
                val pItem = SupplierPurchaseItem(
                    purchaseId = purchaseId,
                    itemId = stockItem.id,
                    itemName = stockItem.name,
                    quantity = qty,
                    unitPrice = buyPrice,
                    currency = itemCurrency
                )
                supplierDao.insertPurchaseItem(pItem)
                
                val existing = inventoryDao.getItemById(stockItem.id)
                if (existing != null) {
                    val newQuantity = existing.quantity + qty
                    val newBuyPrice = if (newQuantity > 0) {
                        ((existing.quantity * existing.buyPrice) + (qty * buyPrice)) / newQuantity
                    } else buyPrice
                    
                    inventoryDao.updateItem(
                        existing.copy(
                            quantity = newQuantity,
                            buyPrice = newBuyPrice,
                            buyPriceCurrency = itemCurrency,
                            dateAdded = existing.dateAdded.ifEmpty { newDate.ifEmpty { Helpers.getCurrentDateTime() } }
                        )
                    )
                } else {
                    inventoryDao.insertItem(
                        InventoryItem(
                            id = stockItem.id,
                            name = stockItem.name,
                            quantity = qty,
                            buyPrice = buyPrice,
                            sellPrice = stockItem.sellPrice,
                            lowStockThreshold = stockItem.lowStockThreshold,
                            buyPriceCurrency = itemCurrency,
                            dateAdded = newDate.ifEmpty { Helpers.getCurrentDateTime() }
                        )
                    )
                }
            }
            
            if (debtTotal > 0.0) {
                supplierDao.updateSupplierBalance(newSupplierId, debtTotal)
            }
        }
    }

    fun getPurchaseItems(purchaseId: Int): Flow<List<SupplierPurchaseItem>> {
        return supplierDao.getPurchaseItems(purchaseId)
    }

    // --- Expense Operations ---
    suspend fun addExpense(expense: Expense): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpenseById(expense.id)
    }

    // --- Transfer Operations ---
    suspend fun addTransfer(transfer: MoneyTransfer): Long {
        return transferDao.insertTransfer(transfer)
    }

    suspend fun deleteTransfer(transfer: MoneyTransfer) {
        transferDao.deleteTransferById(transfer.id)
    }
}
