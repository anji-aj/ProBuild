package com.example.probuild.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientProvider {

    val client = createSupabaseClient(
        supabaseUrl = "YOUR_SUPABASE_PROJECT_URL",
        supabaseKey = "YOUR_SUPABASE_PUBLISHABLE_KEY"
    ) {
        install(Auth)
        install(Postgrest)
    }
}
