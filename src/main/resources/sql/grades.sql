insert
  into time_series_grades (json_data_id,
                           start_time,
                           end_time,
                           grade_code
                          )
select json_data_id,
       adjust_timestamp(jsonb_extract_path_text(grades, 'StartTime')) start_time,
       adjust_timestamp(jsonb_extract_path_text(grades, 'EndTime')) end_time,
       jsonb_extract_path_text(grades, 'GradeCode') grade_code
  from (select json_data_id,
               jsonb_array_elements(jsonb_extract_path(json_content, 'Grades')) grades
          from json_data
         where json_data_id = ?) a
