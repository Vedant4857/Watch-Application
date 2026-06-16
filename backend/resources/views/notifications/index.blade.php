<!DOCTYPE html>
<html>
<head>
    <title>Notifications</title>
</head>
<body>

<h1>Notifications</h1>

@foreach($notifications as $notification)

    <div style="
        border:1px solid #ccc;
        margin:10px;
        padding:10px;
    ">
        <h3>{{ $notification->title }}</h3>

        <p>{{ $notification->message }}</p>

        <small>
            {{ $notification->created_at }}
        </small>
    </div>

@endforeach

</body>
</html>