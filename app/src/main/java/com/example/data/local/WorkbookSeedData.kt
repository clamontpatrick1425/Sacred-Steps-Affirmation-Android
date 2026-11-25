package com.example.data.local

import com.example.domain.model.WorkbookModule
import com.example.domain.model.WorkbookModuleId
import com.example.domain.model.WorkbookPrompt

object WorkbookSeedData {

    val modules: List<WorkbookModule> = listOf(
        WorkbookModule(
            id = WorkbookModuleId.THE_PATH,
            workbookNumber = "Workbook 1",
            title = "The Path",
            subtitle = "12-Step Guided Experience",
            description = "A gentle companion through surrender, trust, and honest living—one sacred step at a time.",
            prompts = listOf(
                WorkbookPrompt(
                    key = "steps_1_3_surrender",
                    title = "Steps 1–3 · Honest Surrender",
                    prompt = "Where have you realized trying harder on your own is no longer enough, and what would it feel like to let grace carry the weight?",
                    placeholder = "Write honestly about what you are ready to hand over..."
                ),
                WorkbookPrompt(
                    key = "steps_4_7_renewal",
                    title = "Steps 4–7 · Humility & Light",
                    prompt = "What old pattern are you ready to look at in the daylight without condemning yourself?",
                    placeholder = "Name one pattern and the truer desire underneath it..."
                ),
                WorkbookPrompt(
                    key = "steps_8_12_walking",
                    title = "Steps 8–12 · Repair & Carrying Hope",
                    prompt = "Who is one person you can offer peace, repair, or encouragement to when the timing is wise?",
                    placeholder = "Note a name, a prayer, or a next gentle step..."
                )
            )
        ),
        WorkbookModule(
            id = WorkbookModuleId.DAILY_WALK,
            workbookNumber = "Workbook 2",
            title = "Daily Walk",
            subtitle = "90-Day Rhythm · Steps 10, 11, 12",
            description = "Simple morning grounding, honest evening check-ins, and quiet ways to stay anchored.",
            prompts = listOf(
                WorkbookPrompt(
                    key = "step_10_evening_review",
                    title = "Step 10 · Evening Grace Check-In",
                    prompt = "Looking back on today without harshness: where did you walk in peace, and where do you need a gentle reset tomorrow?",
                    placeholder = "One honest win and one gentle reset..."
                ),
                WorkbookPrompt(
                    key = "step_11_prayer_stillness",
                    title = "Step 11 · Prayer & Stillness",
                    prompt = "What honest prayer is sitting on your heart right now?",
                    placeholder = "Write a short, unpolished prayer..."
                ),
                WorkbookPrompt(
                    key = "step_12_service",
                    title = "Step 12 · Quiet Service",
                    prompt = "How can you show simple kindness or steady presence to someone else today?",
                    placeholder = "A text, a call, a prayer, or listening ear..."
                )
            )
        ),
        WorkbookModule(
            id = WorkbookModuleId.SHADOWS_TO_LIGHT,
            workbookNumber = "Workbook 3",
            title = "Shadows to Light",
            subtitle = "Healing Shame · Steps 4, 5, 8, 9",
            description = "Extra care for tender places. You are never required to rush through painful memories.",
            traumaInformedNotice = "Go at your own pace. If any memory feels heavy, tap Breathe at the top and step away without guilt.",
            prompts = listOf(
                WorkbookPrompt(
                    key = "shame_vs_truth",
                    title = "Naming the Lie & Speaking Truth",
                    prompt = "When shame whispers an old accusation, what is the exact sentence it uses—and what gentler truth answers it?",
                    placeholder = "The lie says... Yet the truth is..."
                ),
                WorkbookPrompt(
                    key = "safe_compassion",
                    title = "Self-Compassion Pause",
                    prompt = "What did the younger or hurting version of you truly need back then that you can offer yourself with gentleness today?",
                    placeholder = "Write with kindness, as if to a dear friend..."
                ),
                WorkbookPrompt(
                    key = "wise_boundaries",
                    title = "Safe Boundaries Around Repair",
                    prompt = "Before making any difficult amends (Steps 8–9), what boundary or sponsor guidance keeps everyone safe?",
                    placeholder = "Jot down wise boundaries or questions for your sponsor..."
                )
            )
        ),
        WorkbookModule(
            id = WorkbookModuleId.NEW_CREATION,
            workbookNumber = "Workbook 4",
            title = "New Creation",
            subtitle = "Milestones & Gratitude",
            description = "Strictly a space to honor life, growth, and quiet miracles. No focus on struggle here.",
            isCelebratoryOnly = true,
            prompts = listOf(
                WorkbookPrompt(
                    key = "grace_milestones",
                    title = "Milestones Worth Honoring",
                    prompt = "Record any milestone—7 days, 30 days, a calm evening, an honest conversation—that reminds you how far grace has brought you.",
                    placeholder = "Celebrate a moment where new life showed up..."
                ),
                WorkbookPrompt(
                    key = "gratitude_list",
                    title = "Three Quiet Miracles",
                    prompt = "What three ordinary blessings feel sweet to notice right now?",
                    placeholder = "1. ...  2. ...  3. ..."
                ),
                WorkbookPrompt(
                    key = "hope_letter",
                    title = "A Note of Hope",
                    prompt = "What good thing are you looking forward to with open hands?",
                    placeholder = "Write down something hopeful ahead..."
                )
            )
        )
    )
}
