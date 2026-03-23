package org.sparcs.soap.App.Shared.Mocks.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Domain.Models.OTL.SemesterEventDate
import org.sparcs.soap.App.Shared.Extensions.toDate

fun Semester.Companion.mockList(): List<Semester>{
    return listOf(
        //2009년
        Semester(
            year= 2009,
            semesterType= SemesterType.SPRING,
            beginDate= "2009-02-01T15:00:00.000Z".toDate()!!,
            endDate= "2009-05-22T14:59:00.000Z".toDate()!!,
            eventDate= SemesterEventDate(
                registrationPeriodStartDate= null,
                registrationPeriodEndDate= null,
                addDropPeriodEndDate= null,
                dropDeadlineDate= null,
                evaluationDeadlineDate= null,
                gradePostingDate= null
            )
        ),
        Semester(
            year= 2009,
            semesterType= SemesterType.AUTUMN,
            beginDate= "2009-08-31T15:00:00.000Z".toDate()!!,
            endDate= "2009-12-21T14:59:00.000Z".toDate()!!,
            eventDate= SemesterEventDate(
                registrationPeriodStartDate= null,
                registrationPeriodEndDate= null,
                addDropPeriodEndDate= null,
                dropDeadlineDate= null,
                evaluationDeadlineDate= null,
                gradePostingDate= null
            )
        ),
        //2010년
        Semester(
            year = 2010,
            semesterType = SemesterType.SPRING,
            beginDate = "2010-01-31T15:00:00.000Z".toDate()!!,
            endDate = "2010-05-21T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        Semester(
            year = 2010,
            semesterType = SemesterType.AUTUMN,
            beginDate = "2010-08-31T15:00:00.000Z".toDate()!!,
            endDate = "2010-12-21T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        //2011년
        Semester(
            year = 2011,
            semesterType = SemesterType.SPRING,
            beginDate = "2011-02-06T15:00:00.000Z".toDate()!!,
            endDate = "2011-05-27T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        Semester(
            year = 2011,
            semesterType = SemesterType.AUTUMN,
            beginDate = "2011-08-31T15:00:00.000Z".toDate()!!,
            endDate = "2011-12-21T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        //2012년
        Semester(
            year = 2012,
            semesterType = SemesterType.SPRING,
            beginDate = "2012-02-05T15:00:00.000Z".toDate()!!,
            endDate = "2012-05-25T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        Semester(
            year = 2012,
            semesterType = SemesterType.AUTUMN,
            beginDate = "2012-08-31T15:00:00.000Z".toDate()!!,
            endDate = "2012-12-21T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        //2013년
        Semester(
            year = 2013,
            semesterType = SemesterType.SPRING,
            beginDate = "2013-03-01T15:00:00.000Z".toDate()!!,
            endDate = "2013-06-21T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        Semester(
            year = 2013,
            semesterType = SemesterType.AUTUMN,
            beginDate = "2013-09-01T15:00:00.000Z".toDate()!!,
            endDate = "2013-12-20T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        //2014년
        Semester(
            year = 2014,
            semesterType = SemesterType.SPRING,
            beginDate = "2014-03-02T15:00:00.000Z".toDate()!!,
            endDate = "2014-06-20T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        Semester(
            year = 2014,
            semesterType = SemesterType.AUTUMN,
            beginDate = "2014-08-31T15:00:00.000Z".toDate()!!,
            endDate = "2014-12-19T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        //2015년
        Semester(
            year = 2015,
            semesterType = SemesterType.SPRING,
            beginDate = "2015-03-01T15:00:00.000Z".toDate()!!,
            endDate = "2015-06-19T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        Semester(
            year = 2015,
            semesterType = SemesterType.AUTUMN,
            beginDate = "2015-08-30T15:00:00.000Z".toDate()!!,
            endDate = "2015-12-18T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        //2016년
        Semester(
            year = 2016,
            semesterType = SemesterType.SPRING,
            beginDate = "2016-03-01T15:00:00.000Z".toDate()!!,
            endDate = "2016-06-21T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        Semester(
            year = 2016,
            semesterType = SemesterType.AUTUMN,
            beginDate = "2016-08-31T15:00:00.000Z".toDate()!!,
            endDate = "2016-12-21T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        //2017년
        Semester(
            year = 2017,
            semesterType = SemesterType.SPRING,
            beginDate = "2017-02-26T15:00:00.000Z".toDate()!!,
            endDate = "2017-06-16T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        Semester(
            year = 2017,
            semesterType = SemesterType.AUTUMN,
            beginDate = "2017-08-27T15:00:00.000Z".toDate()!!,
            endDate = "2017-12-15T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        //2018년
        Semester(
            year = 2018,
            semesterType = SemesterType.SPRING,
            beginDate = "2018-02-25T15:00:00.000Z".toDate()!!,
            endDate = "2018-06-18T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        Semester(
            year = 2018,
            semesterType = SemesterType.AUTUMN,
            beginDate = "2018-08-26T15:00:00.000Z".toDate()!!,
            endDate = "2018-12-14T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        //2019년
        Semester(
            year = 2019,
            semesterType = SemesterType.SPRING,
            beginDate = "2019-02-24T15:00:00.000Z".toDate()!!,
            endDate = "2019-06-14T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = null,
                registrationPeriodEndDate = null,
                addDropPeriodEndDate = null,
                dropDeadlineDate = null,
                evaluationDeadlineDate = null,
                gradePostingDate = null
            )
        ),
        Semester(
            year = 2019,
            semesterType = SemesterType.AUTUMN,
            beginDate = "2019-09-01T15:00:00.000Z".toDate()!!,
            endDate = "2019-12-20T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = "2019-07-08T03:30:00.000Z".toDate(),
                registrationPeriodEndDate = "2019-07-12T14:59:00.000Z".toDate(),
                addDropPeriodEndDate = "2019-09-09T14:59:00.000Z".toDate(),
                dropDeadlineDate = "2019-10-18T14:59:00.000Z".toDate(),
                evaluationDeadlineDate = "2019-12-13T14:59:00.000Z".toDate(),
                gradePostingDate = "2019-12-30T15:00:00.000Z".toDate()
            )
        ),
        //2020년
        Semester(
            year = 2020,
            semesterType = SemesterType.SPRING,
            beginDate = "2020-03-15T15:00:00.000Z".toDate()!!,
            endDate = "2020-07-03T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = "2020-01-13T03:30:00.000Z".toDate(),
                registrationPeriodEndDate = "2020-01-17T14:59:00.000Z".toDate(),
                addDropPeriodEndDate = "2020-03-27T14:59:00.000Z".toDate(),
                dropDeadlineDate = "2020-05-01T14:59:00.000Z".toDate(),
                evaluationDeadlineDate = "2020-06-26T14:59:00.000Z".toDate(),
                gradePostingDate = "2020-07-09T15:00:00.000Z".toDate()
            )
        ),
        Semester(
            year = 2020,
            semesterType = SemesterType.AUTUMN,
            beginDate = "2020-08-30T15:00:00.000Z".toDate()!!,
            endDate = "2020-12-18T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = "2020-08-10T03:30:00.000Z".toDate(),
                registrationPeriodEndDate = "2020-08-14T14:59:00.000Z".toDate(),
                addDropPeriodEndDate = "2020-09-07T14:59:00.000Z".toDate(),
                dropDeadlineDate = "2020-10-16T14:59:00.000Z".toDate(),
                evaluationDeadlineDate = "2020-12-11T14:59:00.000Z".toDate(),
                gradePostingDate = "2020-12-24T15:00:00.000Z".toDate()
            )
        ),
        //2021년
        Semester(
            year = 2021,
            semesterType = SemesterType.SPRING,
            beginDate = "2021-03-01T15:00:00.000Z".toDate()!!,
            endDate = "2021-06-18T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = "2021-01-11T03:30:00.000Z".toDate(),
                registrationPeriodEndDate = "2021-01-15T14:59:00.000Z".toDate(),
                addDropPeriodEndDate = "2021-03-08T14:59:00.000Z".toDate(),
                dropDeadlineDate = "2021-04-16T14:59:00.000Z".toDate(),
                evaluationDeadlineDate = "2021-06-11T14:59:00.000Z".toDate(),
                gradePostingDate = "2021-06-25T01:00:00.000Z".toDate()
            )
        ),
        Semester(
            year = 2021,
            semesterType = SemesterType.AUTUMN,
            beginDate = "2021-08-29T15:00:00.000Z".toDate()!!,
            endDate = "2021-12-17T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = "2021-08-09T03:30:00.000Z".toDate(),
                registrationPeriodEndDate = "2021-08-13T14:59:00.000Z".toDate(),
                addDropPeriodEndDate = "2021-09-06T14:59:00.000Z".toDate(),
                dropDeadlineDate = "2021-10-15T14:59:00.000Z".toDate(),
                evaluationDeadlineDate = "2021-12-10T14:59:00.000Z".toDate(),
                gradePostingDate = "2021-12-24T01:00:00.000Z".toDate()
            )
        ),
        //2022년
        Semester(
            year = 2022,
            semesterType = SemesterType.SPRING,
            beginDate = "2022-02-27T15:00:00.000Z".toDate()!!,
            endDate = "2022-06-17T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = "2022-01-03T03:30:00.000Z".toDate(),
                registrationPeriodEndDate = "2022-01-07T14:59:00.000Z".toDate(),
                addDropPeriodEndDate = "2022-03-07T14:59:00.000Z".toDate(),
                dropDeadlineDate = "2022-04-15T14:59:00.000Z".toDate(),
                evaluationDeadlineDate = "2022-06-10T14:59:00.000Z".toDate(),
                gradePostingDate = "2022-06-24T01:00:00.000Z".toDate()
            )
        ),
        Semester(
            year = 2022,
            semesterType = SemesterType.AUTUMN,
            beginDate = "2022-08-28T15:00:00.000Z".toDate()!!,
            endDate = "2022-12-16T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = "2022-08-08T03:30:00.000Z".toDate(),
                registrationPeriodEndDate = "2022-08-12T14:59:00.000Z".toDate(),
                addDropPeriodEndDate = "2022-09-05T14:59:00.000Z".toDate(),
                dropDeadlineDate = "2022-10-14T14:59:00.000Z".toDate(),
                evaluationDeadlineDate = "2022-12-09T14:59:00.000Z".toDate(),
                gradePostingDate = "2022-12-23T01:00:00.000Z".toDate()
            )
        ),
        //2023년
        Semester(
            year = 2023,
            semesterType = SemesterType.SPRING,
            beginDate = "2023-02-26T15:00:00.000Z".toDate()!!,
            endDate = "2023-06-16T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = "2023-01-02T03:30:00.000Z".toDate(),
                registrationPeriodEndDate = "2023-01-06T14:59:00.000Z".toDate(),
                addDropPeriodEndDate = "2023-03-06T14:59:00.000Z".toDate(),
                dropDeadlineDate = "2023-04-14T14:59:00.000Z".toDate(),
                evaluationDeadlineDate = "2023-06-09T14:59:00.000Z".toDate(),
                gradePostingDate = "2023-06-23T01:00:00.000Z".toDate()
            )
        ),
        Semester(
            year = 2023,
            semesterType = SemesterType.AUTUMN,
            beginDate = "2023-08-28T15:00:00.000Z".toDate()!!,
            endDate = "2023-12-15T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = "2023-08-07T03:30:00.000Z".toDate(),
                registrationPeriodEndDate = "2023-08-11T14:59:00.000Z".toDate(),
                addDropPeriodEndDate = "2023-09-04T14:59:00.000Z".toDate(),
                dropDeadlineDate = "2023-10-13T14:59:00.000Z".toDate(),
                evaluationDeadlineDate = "2023-12-08T14:59:00.000Z".toDate(),
                gradePostingDate = "2023-12-21T01:00:00.000Z".toDate()
            )
        ),
        //2024년
        Semester(
            year = 2024,
            semesterType = SemesterType.SPRING,
            beginDate = "2024-02-25T15:00:00.000Z".toDate()!!,
            endDate = "2024-06-14T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = "2024-01-08T03:30:00.000Z".toDate(),
                registrationPeriodEndDate = "2024-01-12T14:59:00.000Z".toDate(),
                addDropPeriodEndDate = "2024-03-04T14:59:00.000Z".toDate(),
                dropDeadlineDate = "2024-04-12T14:59:00.000Z".toDate(),
                evaluationDeadlineDate = "2024-06-07T14:59:00.000Z".toDate(),
                gradePostingDate = "2024-06-20T01:00:00.000Z".toDate()
            )
        ),
        Semester(
            year = 2024,
            semesterType = SemesterType.AUTUMN,
            beginDate = "2024-09-01T15:00:00.000Z".toDate()!!,
            endDate = "2024-12-20T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = "2024-08-12T03:30:00.000Z".toDate(),
                registrationPeriodEndDate = "2024-08-16T14:59:00.000Z".toDate(),
                addDropPeriodEndDate = "2024-09-09T14:59:00.000Z".toDate(),
                dropDeadlineDate = "2024-10-18T14:59:00.000Z".toDate(),
                evaluationDeadlineDate = "2024-12-13T14:59:00.000Z".toDate(),
                gradePostingDate = "2024-12-26T01:00:00.000Z".toDate()
            )
        ),
        //2025년
        Semester(
            year = 2025,
            semesterType = SemesterType.SPRING,
            beginDate = "2025-02-23T15:00:00.000Z".toDate()!!,
            endDate = "2025-06-13T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = "2025-01-06T03:30:00.000Z".toDate(),
                registrationPeriodEndDate = "2025-01-10T14:59:00.000Z".toDate(),
                addDropPeriodEndDate = "2025-03-04T14:59:00.000Z".toDate(),
                dropDeadlineDate = "2025-04-11T14:59:00.000Z".toDate(),
                evaluationDeadlineDate = "2025-06-06T14:59:00.000Z".toDate(),
                gradePostingDate = "2025-06-19T01:00:00.000Z".toDate()
            )
        ),
        Semester(
            year = 2025,
            semesterType = SemesterType.AUTUMN,
            beginDate = "2025-08-31T15:00:00.000Z".toDate()!!,
            endDate = "2025-12-19T14:59:00.000Z".toDate()!!,
            eventDate = SemesterEventDate(
                registrationPeriodStartDate = "2025-08-11T03:30:00.000Z".toDate(),
                registrationPeriodEndDate = "2025-08-18T14:59:00.000Z".toDate(),
                addDropPeriodEndDate = "2025-09-08T14:59:00.000Z".toDate(),
                dropDeadlineDate = "2025-10-17T14:59:00.000Z".toDate(),
                evaluationDeadlineDate = "2025-12-12T14:59:00.000Z".toDate(),
                gradePostingDate = "2025-12-26T01:00:00.000Z".toDate()
            )
        )
    )
}