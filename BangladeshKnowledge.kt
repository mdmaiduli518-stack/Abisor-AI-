package com.example.data

import com.example.i18n.AppLanguage

data class KnowledgeTopic(
    val id: String,
    val chipBn: String,
    val chipEn: String,
    val questionBn: String,
    val questionEn: String,
    val answerBn: String,
    val answerEn: String
)

object BangladeshKnowledge {
    val topics = listOf(
        KnowledgeTopic(
            id = "lang_1952",
            chipBn = "১৯৫২ ভাষা আন্দোলন",
            chipEn = "Language Movement 1952",
            questionBn = "১৯৫২ সালের ভাষা আন্দোলন সম্পর্কে সংক্ষেপে বলো।",
            questionEn = "Tell me about the 1952 Language Movement in Bangladesh.",
            answerBn = """
                **১৯৫২-এর ভাষা আন্দোলন**
                • **প্রেক্ষাপট:** ১৯৪৮ সালে পাকিস্তানের তৎকালীন শাসকগোষ্ঠী উর্দুকে একমাত্র রাষ্ট্রভাষা করার ঘোষণা দিলে পূর্ব বাংলার ছাত্র-জনতা এর তীব্র প্রতিবাদ জানায়।
                • **২১শে ফেব্রুয়ারি ১৯৫২:** ঢাকা মেডিকেল কলেজের সামনে ১৪৪ ধারা ভঙ্গ করে মিছিল বের করলে পুলিশ গুলি চালায়। এতে **রফিক, সালাম, বরকত, জব্বার, শফিউর**সহ অনেকে শহিদ হন।
                • **অর্জন:** ১৯৫৬ সালে বাংলাকে পাকিস্তানের অন্যতম রাষ্ট্রভাষার স্বীকৃতি দেওয়া হয়।
                • **আন্তর্জাতিক স্বীকৃতি:** ১৯৯৯ সালে ইউনেস্কো ২১শে ফেব্রুয়ারিকে **আন্তর্জাতিক মাতৃভাষা দিবস** হিসেবে ঘোষণা করে।
            """.trimIndent(),
            answerEn = """
                **Language Movement of 1952 (Bhasha Andolon)**
                • **Background:** After 1948 declarations to make Urdu the sole state language of Pakistan, students and citizens of East Bengal protested for Bangla.
                • **21 February 1952:** Defying Section 144 near Dhaka Medical College, peaceful demonstrators faced police firing. **Salam, Barkat, Rafiq, Jabbar, and Shafiur** embraced martyrdom.
                • **Outcome:** Bangla was recognized as a state language in 1956.
                • **Global Recognition:** In 1999, UNESCO proclaimed 21 February as **International Mother Language Day**.
            """.trimIndent()
        ),
        KnowledgeTopic(
            id = "march_7",
            chipBn = "৭ই মার্চের ভাষণ",
            chipEn = "March 7 Speech",
            questionBn = "৭ই মার্চের ঐতিহাসিক ভাষণ সম্পর্কে জানাও।",
            questionEn = "Explain the significance of the March 7 speech.",
            answerBn = """
                **৭ই মার্চের ঐতিহাসিক ভাষণ (১৯৭১)**
                • **স্থান ও তারিখ:** ১৯৭১ সালের ৭ই মার্চ ঢাকার তৎকালীন **রেসকোর্স ময়দানে** (বর্তমান সোহরাওয়ার্দী উদ্যান) বঙ্গবন্ধু শেখ মুজিবুর রহমান প্রায় ১০ লক্ষ মানুষের সামনে ১৮-১৯ মিনিটের এই ভাষণ দেন।
                • **মূল আহ্বান:** “এবারের সংগ্রাম আমাদের মুক্তির সংগ্রাম, এবারের সংগ্রাম স্বাধীনতার সংগ্রাম।” — এই ঘোষণার মাধ্যমে বাঙালি জাতি মুক্তিযুদ্ধের চূড়ান্ত প্রস্তুতি গ্রহণ করে।
                • **বিশ্ব ঐতিহ্য:** ২০১৭ সালের ৩০শে অক্টোবর ইউনেস্কো এই ভাষণকে **‘মেমোরি অব দ্য ওয়ার্ল্ড ইন্টারন্যাশনাল রেজিস্টার’**-এ বিশ্ব প্রামাণ্য ঐতিহ্য হিসেবে অন্তর্ভুক্ত করে।
            """.trimIndent(),
            answerEn = """
                **Historic March 7 Speech (1971)**
                • **Date & Venue:** Delivered on 7 March 1971 at the Racecourse Maidan (now Suhrawardy Udyan) in Dhaka by Bangabandhu Sheikh Mujibur Rahman before nearly a million people.
                • **Core Message:** The famous declaration — *"The struggle this time is a struggle for our emancipation; the struggle this time is a struggle for independence"* — mobilized the entire nation for liberation.
                • **UNESCO Recognition:** Added to UNESCO's **Memory of the World International Register** in October 2017.
            """.trimIndent()
        ),
        KnowledgeTopic(
            id = "march_26",
            chipBn = "২৬শে মার্চ স্বাধীনতা",
            chipEn = "March 26 Independence",
            questionBn = "২৬শে মার্চ স্বাধীনতা দিবস সম্পর্কে বলো।",
            questionEn = "What is the history of March 26 Independence Day?",
            answerBn = """
                **২৬শে মার্চ — মহান স্বাধীনতা ও জাতীয় দিবস**
                • **অপারেশন সার্চলাইট:** ১৯৭১ সালের ২৫শে মার্চ কালরাতে পাকিস্তানি হানাদার বাহিনী ঢাকাসহ সারা দেশে নিরস্ত্র বাঙালির ওপর গণহত্যা শুরু করে।
                • **স্বাধীনতার ঘোষণা:** ২৬শে মার্চের প্রথম প্রহরে বাংলাদেশের স্বাধীনতার ঘোষণা দেওয়া হয় এবং চট্টগ্রামের কালুরঘাট বেতার কেন্দ্র থেকেও তা প্রচারিত হয়।
                • **তাৎপর্য:** এই দিন থেকেই আনুষ্ঠানিকভাবে বাংলাদেশের ৯ মাসব্যাপী সশস্ত্র মুক্তিযুদ্ধের সূচনা হয়। প্রতি বছর ২৬শে মার্চ জাতীয় স্মৃতিসৌধে শ্রদ্ধা নিবেদনের মাধ্যমে দিনটি পালিত হয়।
            """.trimIndent(),
            answerEn = """
                **26 March — Independence & National Day**
                • **Background:** On the dark night of 25 March 1971, the Pakistan military launched *Operation Searchlight*, a brutal crackdown on unarmed civilians.
                • **Declaration of Independence:** In the early hours of 26 March 1971, Bangladesh's independence was proclaimed, marking the formal birth of the sovereign nation and start of the Liberation War.
                • **Observance:** Celebrated nationwide every year on 26 March with tributes at the National Martyrs' Memorial in Savar.
            """.trimIndent()
        ),
        KnowledgeTopic(
            id = "liberation_war",
            chipBn = "১৯৭১ মুক্তিযুদ্ধ",
            chipEn = "Liberation War 1971",
            questionBn = "১৯৭১ সালের বাংলাদেশের মুক্তিযুদ্ধ সম্পর্কে সংক্ষেপে বলো।",
            questionEn = "Summarize the 1971 Liberation War of Bangladesh.",
            answerBn = """
                **১৯৭১-এর মহান মুক্তিযুদ্ধ**
                • **সময়কাল:** ২৬শে মার্চ থেকে ১৬ই ডিসেম্বর ১৯৭১ (৯ মাস)।
                • **মুজিবনগর সরকার:** ১০ই এপ্রিল ১৯৭১ গঠিত হয় এবং ১৭ই এপ্রিল মেহেরপুরের বৈদ্যনাথতলায় (মুজিবনগর) শপথ গ্রহণ করে।
                • **সেক্টর ও বাহিনী:** মুক্তিযুদ্ধ পরিচালনার জন্য সারা দেশকে **১১টি সেক্টরে** ভাগ করা হয়। মুক্তিবাহিনী ও সাধারণ মানুষের বীরত্বে গড়ে ওঠে দুর্বার প্রতিরোধ।
                • **ত্যাগ ও অর্জন:** ৩০ লক্ষ শহিদের রক্ত ও লক্ষ মা-বোনের আত্মত্যাগের বিনিময়ে অর্জিত হয় স্বাধীন ও সার্বভৌম **গণপ্রজাতন্ত্রী বাংলাদেশ**।
            """.trimIndent(),
            answerEn = """
                **Bangladesh Liberation War (1971)**
                • **Duration:** 26 March to 16 December 1971 (9 months).
                • **Provisional Government:** Formed on 10 April 1971 and sworn in at Mujibnagar (Meherpur) on 17 April 1971.
                • **Organization:** Bangladesh was divided into **11 military sectors** coordinated by the Mukti Bahini alongside brave freedom fighters across the country.
                • **Victory:** Achieved through immense sacrifice to establish the independent People's Republic of Bangladesh.
            """.trimIndent()
        ),
        KnowledgeTopic(
            id = "dec_16",
            chipBn = "১৬ই ডিসেম্বর বিজয় দিবস",
            chipEn = "Victory Day Dec 16",
            questionBn = "১৬ই ডিসেম্বর বিজয় দিবস সম্পর্কে জানাও।",
            questionEn = "Tell me about Victory Day on December 16.",
            answerBn = """
                **১৬ই ডিসেম্বর — মহান বিজয় দিবস**
                • **ঐতিহাসিক মুহূর্ত:** ১৯৭১ সালের ১৬ই ডিসেম্বর বিকেল ৪টা ৩১ মিনিটে ঢাকার রেসকোর্স ময়দানে পাকিস্তানি হানাদার বাহিনীর প্রায় ৯৩,০০০ সৈন্য মুক্তিবাহিনী ও মিত্রবাহিনীর যৌথ কমান্ডের কাছে নিঃশর্ত আত্মসমর্পণ করে।
                • **তাৎপর্য:** ৯ মাসের রক্তক্ষয়ী মুক্তিযুদ্ধের অবসান ঘটে এবং বিশ্বের মানচিত্রে স্বাধীন রাষ্ট্র হিসেবে বাংলাদেশের চূড়ান্ত বিজয় অর্জিত হয়।
                • **উদযাপন:** জাতীয় প্যারেড গ্রাউন্ডে কুচকাওয়াজ, সাভার জাতীয় স্মৃতিসৌধে পুষ্পস্তবক অর্পণ এবং দেশজুড়ে লাল-সবুজের উৎসবে দিনটি উদযাপিত হয়।
            """.trimIndent(),
            answerEn = """
                **16 December — Victory Day (Bijoy Dibosh)**
                • **Historic Surrender:** On 16 December 1971 at 4:31 PM at the Racecourse Maidan in Dhaka, Pakistani forces (~93,000 troops) signed the Instrument of Surrender before the Joint Forces of the Mukti Bahini and Indian Army.
                • **Significance:** Marked the decisive end of the 9-month Liberation War and the birth of a free Bangladesh on the world map.
                • **Celebration:** Observed with parades, floral tributes at Savar National Martyrs' Memorial, and green-and-red celebrations nationwide.
            """.trimIndent()
        ),
        KnowledgeTopic(
            id = "national_anthem",
            chipBn = "জাতীয় সংগীত",
            chipEn = "National Anthem",
            questionBn = "বাংলাদেশের জাতীয় সংগীত সম্পর্কে তথ্য দাও।",
            questionEn = "Tell me about the National Anthem of Bangladesh.",
            answerBn = """
                **বাংলাদেশের জাতীয় সংগীত — ‘আমার সোনার বাংলা’**
                • **রচয়িতা ও সুরকার:** বিশ্বকবি **রবীন্দ্রনাথ ঠাকুর** (১৯০৫ সালে বঙ্গভঙ্গের সময় রচিত; সুর বাউল গায়ক গগন হরকরার গান ‘আমি কোথায় পাব তারে’ থেকে অনুপ্রাণিত)।
                • **গৃহীত অংশ:** গানটির প্রথম **১০ চরণ** বাংলাদেশের জাতীয় সংগীত হিসেবে গৃহীত হয়েছে।
                • **প্রথম চরণ:** *“আমার সোনার বাংলা, আমি তোমায় ভালোবাসি।”*
                • **রাষ্ট্রীয় স্বীকৃতি:** ১৯৭১ সালের মুক্তিযুদ্ধের সময় এটি প্রেরণা জোগায় এবং ১৯৭২ সালের ১৩ই জানুয়ারি আনুষ্ঠানিকভাবে জাতীয় সংগীত হিসেবে গৃহীত হয়।
            """.trimIndent(),
            answerEn = """
                **National Anthem of Bangladesh — "Amar Shonar Bangla"**
                • **Author & Composer:** Written by **Rabindranath Tagore** in 1905 (melody inspired by Baul singer Gagan Harkara's folk tune).
                • **Adopted Stanza:** The **first 10 lines** of the song serve as the official National Anthem.
                • **Opening Line:** *"Amar Shonar Bangla, Ami Tomay Bhalobashi"* ("My Golden Bengal, I love you").
                • **Adoption:** Officially adopted by the Government of Bangladesh on 13 January 1972.
            """.trimIndent()
        ),
        KnowledgeTopic(
            id = "national_flag",
            chipBn = "জাতীয় পতাকা",
            chipEn = "National Flag",
            questionBn = "বাংলাদেশের জাতীয় পতাকার মাপ ও তাৎপর্য কী?",
            questionEn = "Describe the design and meaning of the National Flag of Bangladesh.",
            answerBn = """
                **বাংলাদেশের জাতীয় পতাকা**
                • **অনুপাত ও নকশা:** পতাকার দৈর্ঘ্য ও প্রস্থের অনুপাত **১০ : ৬** (অর্থাৎ ৫ : ৩)। গাঢ় সবুজ (#006a4e) আয়তক্ষেত্রের মাঝে একটি লাল (#f42a41) বৃত্ত থাকে।
                • **বৃত্তের অবস্থান:** বৃত্তের ব্যাসার্ধ পতাকার দৈর্ঘ্যের পাঁচ ভাগের এক ভাগ; এর কেন্দ্র দৈর্ঘ্যের ৯/২০ অংশে (সামান্য বাম দিকে) এবং প্রস্থের ঠিক মাঝখানে অবস্থিত।
                • **তাৎপর্য:**
                  - **গাঢ় সবুজ:** বাংলাদেশের চিরসবুজ প্রকৃতি, শ্যামল বাংলা ও তারুণ্যের প্রতীক।
                  - **লাল বৃত্ত:** ১৯৭১-এর মুক্তিযুদ্ধে শহিদদের রক্ত এবং স্বাধীনতার উদীয়মান সূর্যের প্রতীক।
                • **মূল নকশাকার:** শিবনারায়ণ দাশ (১৯৭১) এবং পরবর্তীতে ১৯৭২ সালে শিল্পী কামরুল হাসান বর্তমান রূপটি চূড়ান্ত করেন।
            """.trimIndent(),
            answerEn = """
                **National Flag of Bangladesh**
                • **Ratio & Design:** Rectangular flag with a **10:6** (5:3) length-to-width ratio, featuring a deep bottle-green field (#006a4e) with a red disc (#f42a41) slightly offset toward the hoist (at 9/20 of the length).
                • **Symbolism:**
                  - **Deep Green:** Represents Bangladesh's lush greenery, vibrant land, and youth.
                  - **Red Disc:** Symbolizes the blood of martyrs sacrificed in 1971 and the rising sun of independence.
                • **Designers:** Originally designed by Shib Narayan Das in 1971 and finalized in its current form by artist Quamrul Hassan in 1972.
            """.trimIndent()
        )
    )

    fun getQuestion(topic: KnowledgeTopic, language: AppLanguage): String =
        if (language == AppLanguage.BENGALI) topic.questionBn else topic.questionEn

    fun getAnswer(topic: KnowledgeTopic, language: AppLanguage): String =
        if (language == AppLanguage.BENGALI) topic.answerBn else topic.answerEn

    fun getChipLabel(topic: KnowledgeTopic, language: AppLanguage): String =
        if (language == AppLanguage.BENGALI) topic.chipBn else topic.chipEn
}
