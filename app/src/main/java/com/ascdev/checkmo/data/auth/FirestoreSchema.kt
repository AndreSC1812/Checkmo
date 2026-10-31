package com.ascdev.checkmo.data.auth

/** Firestore collection and field names. Keep in sync with `firestore.rules`. */
internal object FirestoreSchema {

    object Users {
        const val COLLECTION = "users"
        const val NAME = "name"
        const val EMAIL = "email"
        const val ROLE = "role"
        const val COMPANY_ID = "companyId"
        const val CREATED_AT = "createdAt"
    }

    object Companies {
        const val COLLECTION = "companies"
        const val NAME = "name"
        const val OWNER_ID = "ownerId"
        const val INVITE_CODE = "inviteCode"
        const val CREATED_AT = "createdAt"
    }

    object InviteCodes {
        const val COLLECTION = "inviteCodes"
        const val COMPANY_ID = "companyId"
    }
}
