<?php

namespace App\Providers;

use App\Contracts\SchoolDataProvider;
use App\Services\School\CompanyApiSchoolDataProvider;
use App\Services\School\DatabaseSchoolDataProvider;
use App\Services\School\MockSchoolDataProvider;
use Illuminate\Support\ServiceProvider;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     */
    public function register(): void
    {
        $this->app->bind(SchoolDataProvider::class, function () {
            return match (config('school.data_source')) {
                'database' => new DatabaseSchoolDataProvider,
                'company_api' => new CompanyApiSchoolDataProvider,
                default => new MockSchoolDataProvider,
            };
        });
    }

    /**
     * Bootstrap any application services.
     */
    public function boot(): void
    {
        //
    }
}
