package coms.dypatil.noticeboard.util

import coms.dypatil.noticeboard.App

object SpnListUtil {

    private val preference = App.preference

    fun getAllDeptList(): List<String> = preference.spRemoteDepartements.split(",", ":")

    //[Hod:Teacher:Lab Assistant:Student, Mechanical:Civil:Electrical, Assistant:Head, Member:Head, Assist:Member:Head]
    fun getAllDesiList(): MutableList<String> = getListFromString(preference.spRemoteDesignation)

    fun getTempDesiList(): MutableList<String> = getListFromString(preference.spRemoteDesignation,
            isTeachingCategory = true)

    fun getTeachingDeptList(): MutableList<String> = getListFromString(preference.spRemoteDepartements,
            isTeachingCategory = true)

    fun getYearList(): MutableList<String> = getListFromString(preference.spRemoteYear)

    fun getStudentFilterDept(): List<String> = if (preference.spUserType=="student")
        listOf(preference.spUserDepartment)
    else
        getTeachingDeptList()

    fun getNonTeachingDeptList(): List<String> = getAllDeptList().drop(1)


    fun getStudentFilterYear(): List<List<String>> {
        val list: MutableList<List<String>> = mutableListOf()
        val yearList: MutableList<String> = getYearList()

        for (year in yearList) {
            list.add(year.split(":"))
        }

        return if (preference.spUserType=="faculty")
            list
        else {
            val userDept = preference.spUserDepartment
            listOf(list[getAllDeptList().indexOf(userDept)])
        }
    }

    fun getFacultyFilterDeptList(): List<String> {
        val userType = preference.spUserType
        return if (userType=="student")
            getNoticeDeptList()
        else
            getAllDeptList()
    }

    fun getFacultyFilterDesiList(): List<List<String>> {
        val userType = preference.spUserType
        return if (userType=="student")
            getNoticeDesiList()
        else
            getAllNoticeDesiList().also { list ->
                for (k in IntRange(1, getTeachingDeptList().size - 1)) {
                    list.add(k, list[0])
                }
            }
    }


    fun getNoticeDeptList(): List<String> =
            if (getTeachingDeptList().contains(preference.spUserDepartment) && preference.spUserDesignation=="Student")
                getAllNoticeDeptList()
            else
                listOf(preference.spUserDepartment)

    fun getNoticeDesiList(): List<List<String>> {
        val userDept = preference.spUserDepartment
        return if (getTeachingDeptList().contains(userDept)) {
            if (preference.spUserDesignation=="Student") {
                getAllNoticeDesiList()
            } else {
                listOf(getAllNoticeDesiList()[0])
            }
        } else {
            listOf(getAllNoticeDesiList()[getAllNoticeDeptList().indexOf(userDept)])
        }
    }


    //[Mechanical, Office, Library, Tpo, Gymkhana]
    private fun getAllNoticeDeptList(): List<String> {
        val userDept = preference.spUserDepartment
        var department: List<String> = preference.spRemoteDepartements.split(",")
        if (getTeachingDeptList().contains(userDept)) {
            department = department.drop(1) as MutableList
            department.run {
                add(0, userDept)
            }
        }
        return department
    }

    // [[Hod, Teacher, Lab Assistant, All], [Mechanical, Civil, Electrical, All], [Assistant, Head, All], [Member, Head, All], [Assist, Member, Head, All]]
    private fun getAllNoticeDesiList(): MutableList<List<String>> {
        val desiAll = getAllDesiList()
        val desiAllList = mutableListOf<List<String>>()

        for (i in desiAll) {
            val list = i.split(":") as ArrayList
            list.add("All")
            desiAllList.add(list)
        }
        desiAllList[0] = desiAllList[0].dropLast(2)
        val temp: java.util.ArrayList<String> = desiAllList[0] as ArrayList
        temp.add("All")
        desiAllList[0] = temp
        if (preference.spUserType=="student") {
            desiAllList[1] = listOf(preference.spUserDepartment)
        }
        return desiAllList
    }

    private fun getListFromString(category: String, isTeachingCategory: Boolean = false): MutableList<String> {
        return if (isTeachingCategory)
            category.substring(0, category.indexOf(",")).split(":") as MutableList
        else
            category.split(",") as MutableList<String>
    }

}