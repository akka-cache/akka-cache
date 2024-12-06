// app/components/layouts/PublicLayout.tsx
import { Outlet } from "@remix-run/react";

export default function PublicLayout() {
    return (
      <div className="flex flex-col lg:flex-row min-h-screen">
        <div className="w-full lg:w-1/2">
          <div className="h-full flex items-center justify-center p-12 bg-[var(--mantine-color-dark-0)]">
            <Outlet context="left" />
          </div>
        </div>
        <div className="w-full lg:w-1/2 min-h-screen lg:h-screen">
          <div className="h-full flex items-center justify-center p-12 bg-[var(--mantine-color-dark-1)]">
            <Outlet context="right" />
          </div>
        </div>
      </div>
    );
  }