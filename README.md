# Enlistment Enterprise Ga


#TO DO 
- Solve the race condition for enlistment
   - Possible problem occurs in EnlistController.java, line 76 when user triggers the Enlist functionality, but either sectionRepo.save at line 77 or studentRepo.save at line 78 causes ObjectOptimisticLockingFailureException. 
   - For example, student 1 connects to section 1, and succeeds. However, the way the system flows is upon succeeding at the first try, the flow retries the entire flow once more. As such, student 1, who is already connected to section 1, connects to section 1 again. 
   - As such, this duplicate connection causes the ScheduleConflcit


- Implement createSection_save_to_db  Integration test in SectionsControllerIT
- Fix race condition in createSection_save_to_db that occurs when multiple admins create sections, which bypasses overlapping schedules, room schedules, etc.
- Refactor integration test

    
