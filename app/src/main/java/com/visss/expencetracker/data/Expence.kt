////package com.visss.expencetracker.data
////
////import com.google.firebase.Timestamp
////import com.google.firebase.firestore.PropertyName
////
////data class Expense(
////    val id: String = "",
////    val title: String = "",
////    val amount: Double = 0.0,
////    val category: String = "",
////    val type: ExpenseType = ExpenseType.EXPENSE,
////    @PropertyName("date") val timestamp: Timestamp? = null,
////    val userId: String = "",
////    val currency: String ="INR",
////    val createdAt: Timestamp? = null
////) {
////    val date: String
////        get() = timestamp?.toDate()?.toString() ?: "Unknown Date"
////
////    companion object {
////        fun fromMap(map: Map<String, Any>): Expense {
////            return Expense(
////                id = map["id"] as? String ?: "",
////                title = map["title"] as? String ?: "",
////                amount = (map["amount"] as? Double) ?: 0.0,
////                category = map["category"] as? String ?: "",
////                type = ExpenseType.valueOf(map["type"] as? String ?: "EXPENSE"),
////                timestamp = map["date"] as? Timestamp,
////                userId = map["userId"] as? String ?: "",
////                createdAt = map["createdAt"] as? Timestamp
////            )
////        }
////    }
////}
////
////enum class ExpenseType {
////    INCOME, EXPENSE
////}
////
//
//
//
//
//
//
//
//
//
//
//
//package com.visss.expencetracker.data
//
//import com.google.firebase.Timestamp
//import com.google.firebase.firestore.Exclude
//
//data class Expense(
//    val id: String = "",
//    val title: String = "",
//    val amount: Double = 0.0,
//    val category: String = "",
//    val type: ExpenseType = ExpenseType.EXPENSE,
//    val timestamp: Timestamp = Timestamp.now(),
//    val userId: String = "",
//    val currency: String = "INR"
//) {
//    // Convert to map for Firestore
//    @Exclude
//    fun toMap(): Map<String, Any> {
//        return mapOf(
//            "id" to id,
//            "title" to title,
//            "amount" to amount,
//            "category" to category,
//            "type" to type.name,
//            "timestamp" to timestamp,
//            "userId" to userId,
//            "currency" to currency
//        )
//    }
//
//    companion object {
//        fun fromMap(map: Map<String, Any>): Expense {
//            return try {
//                Expense(
//                    id = map["id"] as? String ?: "",
//                    title = map["title"] as? String ?: "",
//                    amount = (map["amount"] as? Double) ?: (map["amount"] as? Long)?.toDouble() ?: 0.0,
//                    category = map["category"] as? String ?: "",
//                    type = try {
//                        ExpenseType.valueOf(map["type"] as? String ?: "EXPENSE")
//                    } catch (e: Exception) {
//                        ExpenseType.EXPENSE
//                    },
//                    timestamp = map["timestamp"] as? Timestamp ?: Timestamp.now(),
//                    userId = map["userId"] as? String ?: "",
//                    currency = map["currency"] as? String ?: "INR"
//                )
//            } catch (e: Exception) {
//                // Return default expense if parsing fails
//                Expense()
//            }
//        }
//    }
//}
//
//enum class ExpenseType {
//    INCOME, EXPENSE
//}




















package com.visss.expencetracker.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Expense(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val type: ExpenseType = ExpenseType.EXPENSE,
    val timestamp: Timestamp = Timestamp.now(),
    val userId: String = "", // id
    val currency: String = "INR"
) {
    // Convert to map for Firestore
    @Exclude
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "amount" to amount,
            "category" to category,
            "type" to type.name,
            "timestamp" to timestamp,
            "userId" to userId, // Include userId in Firestore
            "currency" to currency
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Expense {
            return try {
                Expense(
                    id = map["id"] as? String ?: "",
                    title = map["title"] as? String ?: "",
                    amount = (map["amount"] as? Double) ?: (map["amount"] as? Long)?.toDouble() ?: 0.0,
                    category = map["category"] as? String ?: "",
                    type = try {
                        ExpenseType.valueOf(map["type"] as? String ?: "EXPENSE")
                    } catch (e: Exception) {
                        ExpenseType.EXPENSE
                    },
                    timestamp = map["timestamp"] as? Timestamp ?: Timestamp.now(),
                    userId = map["userId"] as? String ?: "", // Get userId from Firestore
                    currency = map["currency"] as? String ?: "INR"
                )
            } catch (e: Exception) {
                // Return default expense if parsing fails
                Expense()
            }
        }
    }
}

enum class ExpenseType {
    INCOME, EXPENSE
}