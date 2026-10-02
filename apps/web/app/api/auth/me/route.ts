import { getCurrentUser } from "@/src/lib/auth/session";
import { NextResponse } from "next/server";

export async function GET() {
  const me = await getCurrentUser();
  if (!me) {
    return NextResponse.json({ message: "Authentication required" }, { status: 401 });
  }

  return NextResponse.json(me);
}
