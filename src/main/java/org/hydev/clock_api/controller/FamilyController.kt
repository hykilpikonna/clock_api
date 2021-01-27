package org.hydev.clock_api.controller

import org.hydev.clock_api.*
import org.hydev.clock_api.entity.Family
import org.hydev.clock_api.entity.FamilyRepo
import org.hydev.clock_api.entity.User
import org.hydev.clock_api.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/family")
open class FamilyController(private val userRepo: UserRepository, private val familyRepo: FamilyRepo)
{
    /**
     * Login, check family, and callback
     *
     * @param username
     * @param password
     * @param fid Family ID
     * @param pin Family Pin
     * @param checkInFamily Verify the user is in the family or not
     * @param callback
     * @return Execution result
     */
    private fun family(username: Str, password: Str, fid: Long, pin: Str, checkInFamily: Bool = true, callback: (User, Family) -> Any?): Any
    {
        return userRepo.login(username, password) { user ->

            val family = familyRepo.findByIdOrNull(fid) ?: return@login "Family not found".http(404)

            // Verify
            if (checkInFamily && user.family != fid) return@login "You're not in this family.".http(400)
            if (family.pinHash != pinHash(fid, pin)) return@login "Family pin incorrect.".http(401)

            // Done
            callback(user, family) ?: "Success"
        }
    }

    @PostMapping("/get")
    fun get(@H username: Str, @H password: Str): Any
    {
        return userRepo.login(username, password)
        {
            if (it.family == null) "You didn't join any family".http(404)
            else familyRepo.findByIdOrNull(it.family)!!
        }
    }

    @PostMapping("/create")
    fun create(@H username: Str, @H password: Str, @H name: Str, @H pin: Str): Any
    {
        return userRepo.login(username, password) { user ->

            // Verify that the user isn't already in a family
            if (user.family != null) return@login "You already have a family.".http(400)

            // Create family and save once to get ID
            var family = Family(name = name, pinHash = "", members = username)
            family = familyRepo.save(family)

            // Create pin hash using id and save again
            family.pinHash = pinHash(family.fid, pin)
            familyRepo.save(family)

            // Set user's family
            user.family = family.fid
            userRepo.save(user)

            // Done
            family
        }
    }

    @PostMapping("/update_pin")
    fun updatePin(@H username: Str, @H password: Str, @H fid: Long, @H pin: Str, @H newPin: Str): Any
    {
        return family(username, password, fid, pin) { _, family ->

            // Change pin
            family.pinHash = pinHash(family.fid, newPin)
            familyRepo.save(family)
        }
    }

    @PostMapping("/action")
    fun action(@H username: Str, @H password: Str, @H fid: Long, @H pin: Str, @H action: Str): Any
    {
        return family(username, password, fid, pin, checkInFamily = action.toLowerCase() != "join") { user, family ->

            // Do action
            when (action.toLowerCase())
            {
                "join" ->
                {
                    if (user.family != null) return@family "You're already in a family.".http(400)
                    user.family = fid
                    userRepo.save(user)
                    family.members = family.members.csv().apply { add(username) }.csv()
                    familyRepo.save(family)
                }
                "delete" ->
                {
                    // TODO: Test this
                    family.members.csv().forEach {
                        val member = userRepo.queryByUsername(it) ?: return@forEach
                        member.family = null
                        userRepo.save(member)
                    }
                    familyRepo.delete(family)
                    family
                }
                "leave" ->
                {
                    // TODO: Test this
                    user.family = null
                    userRepo.save(user)
                    family.members = family.members.csv().apply { remove(username) }.csv()
                    familyRepo.save(family)
                }
                else -> "".http(404)
            }
        }
    }

    @PostMapping("/add_alarm")
    fun addAlarm(@H username: Str, @H password: Str, @H fid: Long, @H pin: Str, @H to: Str, @H alarm: Str): Any
    {
        return family(username, password, fid, pin) { _, _ ->

            val toUser = userRepo.queryByUsername(to) ?: return@family "User not found in database".http(404)
            if (toUser.family != fid) return@family "User not in your family".http(401)
            
            toUser.notifications = toUser.notifications.csv().apply { add(alarm) }.csv()
            userRepo.save(toUser)
            "Success"
        }
    }

    @PostMapping("/get_alarm_updates")
    fun getUpdates(@H username: Str, @H password: Str): Any
    {
        return userRepo.login(username, password) { user ->

            // TODO: test this
            val response = user.notifications
            user.notifications = ""
            userRepo.save(user)
            response
        }
    }


}
