import { authenticatedApiFetch } from "@/src/lib/auth/session";
import { NextResponse, type NextRequest } from "next/server";

type Context = {
  params: Promise<{ path: string[] }>;
};

export async function GET(request: NextRequest, context: Context) {
  return proxy(request, context);
}

export async function POST(request: NextRequest, context: Context) {
  return proxy(request, context);
}

export async function PUT(request: NextRequest, context: Context) {
  return proxy(request, context);
}

export async function PATCH(request: NextRequest, context: Context) {
  return proxy(request, context);
}

export async function DELETE(request: NextRequest, context: Context) {
  return proxy(request, context);
}

async function proxy(request: NextRequest, context: Context) {
  const { path } = await context.params;
  const apiPath = `/api/v1/${path.join("/")}${request.nextUrl.search}`;
  const method = request.method;
  const hasBody = method !== "GET" && method !== "HEAD";
  const body = hasBody ? await request.text() : undefined;

  const response = await authenticatedApiFetch(apiPath, {
    method,
    body: body || undefined,
  });

  if (response.status === 204) {
    return new NextResponse(null, { status: 204 });
  }

  const text = await response.text();
  const headers = new Headers();
  const contentType = response.headers.get("content-type");
  if (contentType) {
    headers.set("content-type", contentType);
  }
  return new NextResponse(text, { status: response.status, headers });
}
