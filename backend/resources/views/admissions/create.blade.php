<!DOCTYPE html>
<html>
<head>
    <title>Add Admission</title>
</head>
<body>

<h1>Add Admission</h1>

<form method="POST" action="/admissions/store">
    @csrf

    <p>
        Student Name
        <br>
        <input type="text" name="student_name">
    </p>

    <p>
        Class
        <br>
        <input type="text" name="class_name">
    </p>

    <p>
        Enrollment No.
        <br>
        <input type="text" name="enrollment_no">
    </p>

    <p>
        Parent Name
        <br>
        <input type="text" name="parent_name">
    </p>

    <button type="submit">
        Add Admission
    </button>

</form>

</body>
</html>