package com.resolver.util_impl

import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.resolver.resolution_logic_api.AwardBehaviour
import com.resolver.util_api.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.serialization.json.Json
import org.icpclive.cds.api.ContestState
import java.nio.file.Path

open class AppBase(
    private val calculator: ScoreboardCalculator,
    private val yesNoConsoleHandler: YesNoConsoleHandler,
    private val json: Json,
    private val block: suspend (
        Passwords,
        Map<String, AwardBehaviour>,
        ResolverOptions,
        frozen: List<ContestState>,
        notFrozen: List<ContestState>
    ) -> Unit
) : App() {
    private val resolverOptions by ResolverCommandLineOptionsImpl()
    private val awardsBehaviourPath: Path
        get() = resolverOptions.configDirectory.resolve(Constants.AWARDS_BEHAVIOUR_FILE_NAME)
    private val resolverOptionsPath: Path
        get() = resolverOptions.configDirectory.resolve(Constants.RESOLVER_OPTIONS_FILE_NAME)
    private val resolverPasswordsPath: Path
        get() = resolverOptions.configDirectory.resolve(Constants.RESOLVER_PASSWORDS_FILE_NAME)

    override fun run() {
        val contestStatesLoader = ContestStatesLoaderImpl(resolverOptions)
        val awardsOptionHandler = AwardsOptionHandlerImpl(
            calculator = calculator,
            contestStatesLoader = contestStatesLoader,
            yesNoConsoleHandler = yesNoConsoleHandler,
            json = json
        )
        val resolverOptionsHandler = ResolverOptionsHandlerImpl(
            json = json,
            yesNoConsoleHandler = yesNoConsoleHandler
        )
        runBlocking {
            awardsOptionHandler.handleGenAwardsOption(
                scope = this,
                isGenAwardsOptionEnabled = resolverOptions.genAwards,
                awardsBehaviourPath = awardsBehaviourPath,
                isAnotherGenNeeded = resolverOptions.genResolverOptions
            )

            resolverOptionsHandler.handleGenResolverOptionsOption(
                isGenResolverOptionsOptionEnabled = resolverOptions.genResolverOptions,
                resolverOptionsPath = resolverOptionsPath
            )

            val passwords = PasswordsLoaderImpl(json).loadPasswords(resolverPasswordsPath)

            val frozen = mutableListOf<ContestState>()
            val notFrozen = mutableListOf<ContestState>()
            val semaphore = Semaphore(2, 2)

            val awardIdToBehaviour = awardsOptionHandler.getAwardIdToBehaviour(
                awardsBehaviourPath = awardsBehaviourPath
            )

            val merged = resolverOptionsHandler.getResolverOptions(
                resolverOptionsPath = resolverOptionsPath
            ).mergeWithCommandLineOptions(resolverOptions)

            val frozenJob = contestStatesLoader.loadContestStates(
                this,
                dst = frozen,
                semaphore = semaphore,
                submissionResultsAfterFreezeInput = false
            )

            val notFrozenJob = contestStatesLoader.loadContestStates(
                scope = this,
                dst = notFrozen,
                semaphore = semaphore,
                submissionResultsAfterFreezeInput = true
            )

            semaphore.acquire()
            semaphore.acquire()

            frozenJob.cancel()
            notFrozenJob.cancel()

            block(
                passwords,
                awardIdToBehaviour,
                merged,
                frozen,
                notFrozen
            )
        }
    }
}